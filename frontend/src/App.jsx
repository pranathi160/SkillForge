
import { useCallback, useEffect, useState } from "react";
import api from "./api";
import Login from "./Login";
import "./App.css";

const EMPTY_CHALLENGE = {
  title: "",
  description: "",
  category: "Data Structures & Algorithms",
  difficulty: "MEDIUM",
  points: "20",
  expectedAnswer: ""
};

export default function App() {
  const [token, setToken] = useState(() => localStorage.getItem("Token"));
  const [page, setPage] = useState("Dashboard");

  const [progress, setProgress] = useState(null);
  const [profile, setProfile] = useState(null);
  const [leaderboard, setLeaderboard] = useState([]);
  const [challenges, setChallenges] = useState([]);
  const [submissions, setSubmissions] = useState([]);

  const [selectedChallenge, setSelectedChallenge] = useState(null);
  const [answer, setAnswer] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const [challengeForm, setChallengeForm] = useState(EMPTY_CHALLENGE);
  const [creatingChallenge, setCreatingChallenge] = useState(false);
  const [adminMessage, setAdminMessage] = useState("");
  const [adminError, setAdminError] = useState(false);

  const storedRole = (
    profile?.role || localStorage.getItem("UserRole") || ""
  ).toUpperCase();
  const isAdmin = storedRole === "ADMIN";

  const loadPublicData = useCallback(async () => {
    try {
      const [leaderboardRes, challengesRes] = await Promise.all([
        api.get("/progress/leaderboard"),
        api.get("/challenges")
      ]);

      setLeaderboard(
        Array.isArray(leaderboardRes.data) ? leaderboardRes.data : []
      );

      setChallenges(
        Array.isArray(challengesRes.data) ? challengesRes.data : []
      );
    } catch (error) {
      console.error("Could not load public data:", error);
    }
  }, []);

  const loadPrivateData = useCallback(async () => {
    if (!localStorage.getItem("Token")) return;

    setLoading(true);

    try {
      const currentRole = (
        localStorage.getItem("UserRole") || ""
      ).toUpperCase();

      const requests = [
        api.get("/users/me"),
        api.get("/submissions/my")
      ];

      if (currentRole !== "ADMIN") {
        requests.push(api.get("/progress/my"));
      }

      const results = await Promise.allSettled(requests);

      if (results[0].status === "fulfilled") {
        const user = results[0].value.data;
        setProfile(user);

        if (user?.role) {
          localStorage.setItem(
            "UserRole",
            String(user.role).toUpperCase()
          );
        }
      }

      if (results[1].status === "fulfilled") {
        setSubmissions(
          Array.isArray(results[1].value.data)
            ? results[1].value.data
            : []
        );
      }

      if (currentRole !== "ADMIN" && results[2]?.status === "fulfilled") {
        setProgress(results[2].value.data);
      }

      results.forEach((result, index) => {
        if (result.status === "rejected") {
          console.error(`Private request ${index + 1} failed:`, result.reason);
        }
      });
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadPublicData();
  }, [loadPublicData]);

  useEffect(() => {
    if (token) {
      loadPrivateData();
    }
  }, [token, loadPrivateData]);

  function handleLogin(newToken) {
    setToken(newToken);
    setPage("Dashboard");
  }

  function handleLogout() {
    localStorage.removeItem("Token");
    localStorage.removeItem("UserEmail");
    localStorage.removeItem("UserRole");
    localStorage.removeItem("UserId");

    setToken(null);
    setProgress(null);
    setProfile(null);
    setSubmissions([]);
    setSelectedChallenge(null);
    setAnswer("");
    setMessage("");
    setAdminMessage("");
    setPage("Dashboard");
  }

  async function handleSubmit(event) {
    event.preventDefault();

    if (isAdmin) {
      setMessage("Admins manage challenges; sign in as a learner to submit.");
      return;
    }

    if (!selectedChallenge || !answer.trim()) return;

    const challengeId =
      selectedChallenge.id ?? selectedChallenge.challengeId;

    if (challengeId == null) {
      setMessage("Challenge ID is missing.");
      return;
    }

    setSubmitting(true);
    setMessage("");

    try {
      const response = await api.post(
        "/submissions",
        {
          challengeId: Number(challengeId),
          answer: answer.trim()
        },
        {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      );

      const result = response.data || {};

      setMessage(
        `Status: ${result.status || "Submitted"} · Score: ${result.score ?? 0}`
      );

      await Promise.all([loadPrivateData(), loadPublicData()]);
    } catch (error) {
      console.error("Submission error:", error.response || error);

      setMessage(
        error.response?.data?.message ||
        (error.response?.status === 401
          ? "Your session expired. Please sign in again."
          : error.response?.status === 403
            ? "This account cannot submit challenges."
            : "Submission failed. Please try again.")
      );
    } finally {
      setSubmitting(false);
    }
  }

  function updateChallengeField(event) {
    const { name, value } = event.target;

    setChallengeForm(previous => ({
      ...previous,
      [name]: value
    }));
  }

  async function handleCreateChallenge(event) {
    event.preventDefault();
    setAdminMessage("");
    setAdminError(false);

    if (!token) {
      setAdminError(true);
      setAdminMessage("Please sign in again.");
      return;
    }

    if (storedRole !== "ADMIN") {
      setAdminError(true);
      setAdminMessage("Only administrators can create challenges.");
      return;
    }

    const payload = {
      title: challengeForm.title.trim(),
      description: challengeForm.description.trim(),
      category: challengeForm.category.trim(),
      difficulty: challengeForm.difficulty,
      points: Number(challengeForm.points),
      expectedAnswer: challengeForm.expectedAnswer.trim()
    };

    if (
      !payload.title ||
      !payload.description ||
      !payload.category ||
      !payload.expectedAnswer ||
      !Number.isFinite(payload.points) ||
      payload.points <= 0
    ) {
      setAdminError(true);
      setAdminMessage("Complete every field and enter positive points.");
      return;
    }

    setCreatingChallenge(true);

    try {
      const response = await api.post("/challenges", payload, {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json"
        }
      });

      setAdminMessage(
        `Challenge created successfully! ID: ${response.data?.id ?? "created"}`
      );
      setAdminError(false);
      setChallengeForm(EMPTY_CHALLENGE);

      await loadPublicData();
    } catch (error) {
      console.error(
        "Challenge creation failed:",
        error.response?.status,
        error.response?.data || error.message
      );

      const status = error.response?.status;

      setAdminError(true);
      setAdminMessage(
        error.response?.data?.message ||
        (status === 401
          ? "Your session expired. Sign in again."
          : status === 403
            ? "403 Forbidden: the backend did not authorize this admin request. Verify the account role, JWT, and SecurityConfig."
            : status === 400
              ? "Invalid challenge details. Check all fields and difficulty."
              : "Could not create the challenge. Check the backend console.")
      );
    } finally {
      setCreatingChallenge(false);
    }
  }

  if (!token) {
    return <Login onLogin={handleLogin} />;
  }

  const name = profile?.fullName || progress?.fullName || "SkillForge User";

  const navItems = [
    ["Dashboard", "◈"],
    ["Challenges", "⌘"],
    ["Leaderboard", "♛"],
    ["My Submissions", "▤"],
    ["My Profile", "◉"]
  ];

  if (isAdmin) {
    navItems.push(["Admin Dashboard", "⚙"]);
  }

  return (
    <div className="sf-app">
      <aside className="sf-sidebar">
        <div className="sf-brand">
          <div className="sf-logo">S</div>
          <div>
            <h2>SkillForge</h2>
            <span>LEARN · BUILD · GROW</span>
          </div>
        </div>

        <p className="sf-nav-label">WORKSPACE</p>

        <nav>
          {navItems.map(([label, icon]) => (
            <button
              type="button"
              key={label}
              className={`sf-nav-item ${page === label ? "active" : ""}`}
              onClick={() => setPage(label)}
            >
              <span>{icon}</span>
              {label}
            </button>
          ))}
        </nav>

        <div className="sf-sidebar-bottom">
          <div className="sf-mini-avatar">
            {name.charAt(0).toUpperCase()}
          </div>
          <div className="sf-user-mini">
            <strong>{name}</strong>
            <span>{storedRole || "USER"}</span>
          </div>
          <button
            type="button"
            className="sf-logout"
            onClick={handleLogout}
            title="Sign out"
          >
            ↗
          </button>
        </div>
      </aside>

      <main className="sf-main">
        <header className="sf-topbar">
          <div>
            <p className="sf-eyebrow">YOUR LEARNING SPACE</p>
            <h1>{page}</h1>
          </div>

          <button
            type="button"
            className="sf-refresh"
            onClick={() => Promise.all([loadPublicData(), loadPrivateData()])}
          >
            ↻ Refresh
          </button>
        </header>

        {loading && <p className="sf-muted">Refreshing your data...</p>}

        {page === "Dashboard" && (
          <>
            <section className="sf-welcome">
              <div>
                <span className="sf-pill">YOUR JOURNEY CONTINUES</span>
                <h2>Welcome back, {name.split(" ")[0]}! ✨</h2>
                <p>
                  {isAdmin
                    ? "Manage coding challenges and help learners grow."
                    : "Keep practising. Every challenge takes you one step forward."}
                </p>
                <button
                  type="button"
                  className="sf-primary"
                  onClick={() => setPage(isAdmin ? "Admin Dashboard" : "Challenges")}
                >
                  {isAdmin ? "Manage challenges →" : "Explore challenges →"}
                </button>
              </div>
              <div className="sf-welcome-art">{"</>"}</div>
            </section>

            <section className="sf-stats">
              <StatCard
                icon="✦"
                label="TOTAL POINTS"
                value={progress?.totalScore ?? 0}
                color="purple"
              />
              <StatCard
                icon="✓"
                label="PASSED CHALLENGES"
                value={progress?.passedChallenges ?? 0}
                color="green"
              />
              <StatCard
                icon="⌘"
                label="TOTAL SUBMISSIONS"
                value={progress?.totalSubmissions ?? submissions.length}
                color="blue"
              />
              <StatCard
                icon="⚡"
                label="AVAILABLE CHALLENGES"
                value={challenges.length}
                color="orange"
              />
            </section>

            <section className="sf-panel">
              <div className="sf-panel-heading">
                <div>
                  <h2>{isAdmin ? "Active challenges" : "Continue learning"}</h2>
                  <p>
                    {isAdmin
                      ? "Review the challenges currently published."
                      : "Pick a challenge and practise your skills."}
                  </p>
                </div>
                <button
                  type="button"
                  className="sf-text-button"
                  onClick={() => setPage("Challenges")}
                >
                  View all →
                </button>
              </div>

              {challenges.slice(0, 3).map(challenge => (
                <ChallengeCard
                  key={challenge.id ?? challenge.challengeId}
                  challenge={challenge}
                  onSolve={() => {
                    setSelectedChallenge(challenge);
                    setAnswer("");
                    setMessage("");
                    setPage("Challenges");
                  }}
                />
              ))}

              {challenges.length === 0 && (
                <p className="sf-muted">No active challenges available.</p>
              )}
            </section>

            <LeaderboardPanel
              leaderboard={leaderboard.slice(0, 5)}
              onViewAll={() => setPage("Leaderboard")}
            />
          </>
        )}

        {page === "Challenges" && (
          <section className="sf-panel">
            <div className="sf-panel-heading">
              <div>
                <h2>Challenge library</h2>
                <p>
                  {isAdmin
                    ? "Review active challenges."
                    : "Choose a challenge and submit your answer."}
                </p>
              </div>
            </div>

            {challenges.map(challenge => (
              <ChallengeCard
                key={challenge.id ?? challenge.challengeId}
                challenge={challenge}
                onSolve={() => {
                  setSelectedChallenge(challenge);
                  setAnswer("");
                  setMessage("");
                }}
              />
            ))}

            {challenges.length === 0 && (
              <p className="sf-muted">No active challenges available.</p>
            )}

            {!isAdmin && selectedChallenge && (
              <form className="sf-submit-form" onSubmit={handleSubmit}>
                <h2>
                  Submit: {selectedChallenge.title || selectedChallenge.name}
                </h2>
                <p className="sf-muted">
                  {selectedChallenge.description}
                </p>
                <label htmlFor="challenge-answer">Your answer</label>
                <textarea
                  id="challenge-answer"
                  value={answer}
                  onChange={event => setAnswer(event.target.value)}
                  placeholder="Write your code or answer..."
                  rows={6}
                  required
                />
                <div className="sf-form-actions">
                  <button className="sf-primary" disabled={submitting}>
                    {submitting ? "Submitting..." : "Submit answer"}
                  </button>
                  <button
                    type="button"
                    className="sf-secondary"
                    onClick={() => {
                      setSelectedChallenge(null);
                      setMessage("");
                    }}
                  >
                    Cancel
                  </button>
                </div>
                {message && <p className="sf-message">{message}</p>}
              </form>
            )}
          </section>
        )}

        {page === "Leaderboard" && (
          <LeaderboardPanel leaderboard={leaderboard} />
        )}

        {page === "My Submissions" && (
          <section className="sf-panel">
            <h2>Submission history</h2>
            <p className="sf-muted">Your attempts returned by the backend.</p>

            {submissions.length === 0 ? (
              <p>No submissions found yet.</p>
            ) : (
              <div className="sf-table-wrap">
                <table className="sf-table">
                  <thead>
                    <tr>
                      <th>Challenge</th>
                      <th>Status</th>
                      <th>Score</th>
                      <th>Submitted</th>
                    </tr>
                  </thead>
                  <tbody>
                    {submissions.map((item, index) => (
                      <tr key={item.id ?? `${item.challengeId}-${index}`}>
                        <td>
                          {item.challengeTitle ||
                            `Challenge ${item.challengeId ?? ""}`}
                        </td>
                        <td>
                          <span className={`sf-status ${(item.status || "").toLowerCase()}`}>
                            {item.status || "UNKNOWN"}
                          </span>
                        </td>
                        <td>{item.score ?? 0}</td>
                        <td>
                          {item.submittedAt
                            ? new Date(item.submittedAt).toLocaleString()
                            : "—"}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        )}

        {page === "My Profile" && (
          <section className="sf-panel sf-profile">
            <div className="sf-profile-avatar">
              {name.charAt(0).toUpperCase()}
            </div>
            <h2>{profile?.fullName || "Loading profile..."}</h2>
            <p className="sf-muted">{profile?.email || ""}</p>

            <div className="sf-profile-details">
              <ProfileRow label="User ID" value={profile?.id} />
              <ProfileRow label="Role" value={profile?.role || storedRole} />
              <ProfileRow
                label="Account status"
                value={profile?.active == null ? "—" : profile.active ? "Active" : "Inactive"}
              />
              <ProfileRow
                label="Member since"
                value={
                  profile?.createdAt
                    ? new Date(profile.createdAt).toLocaleDateString()
                    : "—"
                }
              />
            </div>
          </section>
        )}

        {page === "Admin Dashboard" && isAdmin && (
          <section className="sf-panel">
            <div className="sf-panel-heading">
              <div>
                <h2>Create a challenge</h2>
                <p>Add a new challenge to the active challenge library.</p>
              </div>
            </div>

            <form
              className="sf-admin-form"
              onSubmit={handleCreateChallenge}
            >
              <label>
                Challenge title
                <input
                  name="title"
                  value={challengeForm.title}
                  onChange={updateChallengeField}
                  placeholder="e.g. Minimum Cost Path in a Grid"
                  required
                />
              </label>

              <label>
                Description
                <textarea
                  name="description"
                  value={challengeForm.description}
                  onChange={updateChallengeField}
                  rows={4}
                  placeholder="Explain the problem and requirements."
                  required
                />
              </label>

              <label>
                Category
                <input
                  name="category"
                  value={challengeForm.category}
                  onChange={updateChallengeField}
                  placeholder="e.g. Graphs & Algorithms"
                  required
                />
              </label>

              <div className="sf-admin-grid">
                <label>
                  Difficulty
                  <select
                    name="difficulty"
                    value={challengeForm.difficulty}
                    onChange={updateChallengeField}
                  >
                    <option value="EASY">Easy</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HARD">Hard</option>
                  </select>
                </label>

                <label>
                  Points
                  <input
                    name="points"
                    type="number"
                    min="1"
                    value={challengeForm.points}
                    onChange={updateChallengeField}
                    required
                  />
                </label>
              </div>

              <label>
                Expected answer
                <textarea
                  name="expectedAnswer"
                  value={challengeForm.expectedAnswer}
                  onChange={updateChallengeField}
                  rows={3}
                  placeholder="Answer used by your backend grading logic."
                  required
                />
              </label>

              <button
                className="sf-primary"
                type="submit"
                disabled={creatingChallenge}
              >
                {creatingChallenge ? "Creating..." : "Create challenge"}
              </button>

              {adminMessage && (
                <p className={adminError ? "sf-error" : "sf-success"} role="status">
                  {adminMessage}
                </p>
              )}
            </form>
          </section>
        )}

        <footer className="sf-footer">
          SkillForge · Keep learning. Keep building.
        </footer>
      </main>
    </div>
  );
}

function StatCard({ icon, label, value, color }) {
  return (
    <article className="sf-stat-card">
      <div className={`sf-stat-icon ${color}`}>{icon}</div>
      <p>{label}</p>
      <h2>{value}</h2>
    </article>
  );
}

function ChallengeCard({ challenge, onSolve }) {
  const title =
    challenge.title ||
    challenge.name ||
    challenge.challengeName ||
    `Challenge ${challenge.id ?? challenge.challengeId}`;

  return (
    <article className="sf-challenge-card">
      <div className="sf-challenge-icon">&lt;/&gt;</div>
      <div className="sf-challenge-content">
        <h3>{title}</h3>
        <p>{challenge.description || "Practice your coding skills."}</p>
        <span className="sf-points">
          ✦ {challenge.points ?? challenge.score ?? 0} points
        </span>
        {challenge.difficulty && (
          <span className="sf-difficulty"> · {challenge.difficulty}</span>
        )}
      </div>
      <button type="button" className="sf-secondary" onClick={onSolve}>
        View →
      </button>
    </article>
  );
}

function LeaderboardPanel({ leaderboard, onViewAll }) {
  return (
    <section className="sf-panel">
      <div className="sf-panel-heading">
        <div>
          <h2>Leaderboard 🏆</h2>
          <p>See how learners are progressing.</p>
        </div>
        {onViewAll && (
          <button
            type="button"
            className="sf-text-button"
            onClick={onViewAll}
          >
            View all →
          </button>
        )}
      </div>

      {leaderboard.length === 0 ? (
        <p className="sf-muted">No leaderboard data available.</p>
      ) : (
        leaderboard.map((user, index) => (
          <div className="sf-leader-row" key={user.userId ?? user.id ?? index}>
            <div className="sf-rank">{user.rank ?? index + 1}</div>
            <div className="sf-leader-name">
              <strong>{user.fullName || user.name || "Learner"}</strong>
              <span>
                {user.passedChallenges ?? 0} passed · {user.totalSubmissions ?? 0} submissions
              </span>
            </div>
            <div className="sf-leader-score">
              {user.totalScore ?? user.score ?? 0} pts
            </div>
          </div>
        ))
      )}
    </section>
  );
}

function ProfileRow({ label, value }) {
  return (
    <div className="sf-profile-row">
      <span>{label}</span>
      <strong>{value ?? "—"}</strong>
    </div>
  );
}
