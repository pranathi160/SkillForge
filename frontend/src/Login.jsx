import { useState } from "react";
import axios from "axios";
import "./Login.css";

const API = import.meta.env.VITE_API_URL || "http://localhost:8081";

export default function Login({ onLogin }) {
  const [role, setRole] = useState("");
  const [mode, setMode] = useState("login");
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  function selectRole(selectedRole) {
    setRole(selectedRole);
    setMode("login");
    setMessage("");
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setMessage("");
    setLoading(true);

    try {
      if (mode === "register") {
        if (role !== "LEARNER") {
          setMessage("Only learners can create accounts.");
          return;
        }

        await axios.post(`${API}/auth/register`, {
          fullName: fullName.trim(),
          email: email.trim(),
          password
        });

        setMessage("Account created successfully. Please sign in.");
        setMode("login");
        setPassword("");
        return;
      }

      const response = await axios.post(`${API}/auth/login`, {
        email: email.trim(),
        password
      });

      const data = response.data;
      const actualRole = String(data.role || "").toUpperCase();

      if (
        !data.token ||
        !data.refreshToken ||
        !["ADMIN", "LEARNER"].includes(actualRole)
      ) {
        setMessage(
          "Login response is missing a valid token, refresh token, or role."
        );
        return;
      }

      if (actualRole !== role) {
        setMessage(
          `This account is registered as ${actualRole}. Please choose ${actualRole}.`
        );
        return;
      }

      localStorage.setItem("Token", data.token);
      localStorage.setItem("RefreshToken", data.refreshToken);
      localStorage.setItem("UserEmail", data.email || email.trim());
      localStorage.setItem("UserRole", actualRole);

      if (data.userId != null) {
        localStorage.setItem("UserId", String(data.userId));
      }

      if (data.fullName) {
        localStorage.setItem("FullName", data.fullName);
      }

      onLogin(data.token);
    } catch (error) {
      console.error("SkillForge login error:", error);

      setMessage(
        error.response?.data?.message ||
        error.response?.data?.error ||
        (error.response
          ? `Request failed with status ${error.response.status}.`
          : "Cannot connect to the backend. Please try again.")
      );
    } finally {
      setLoading(false);
    }
  }

  function goBack() {
    setRole("");
    setMode("login");
    setMessage("");
    setPassword("");
  }

  if (!role) {
    return (
      <main className="sf-auth-page">
        <section className="sf-auth-card">
          <div className="sf-logo">S</div>
          <h1>SkillForge</h1>
          <p className="sf-subtitle">
            Learn, practice, and grow your skills.
          </p>

          <h2>Continue as</h2>

          <button
            type="button"
            className="sf-primary"
            onClick={() => selectRole("LEARNER")}
          >
            🎓 Learner
          </button>

          <button
            type="button"
            className="sf-secondary"
            onClick={() => selectRole("ADMIN")}
          >
            🛡️ Admin
          </button>

          <p className="sf-note">
            Admin access requires an existing administrator account.
          </p>
        </section>
      </main>
    );
  }

  return (
    <main className="sf-auth-page">
      <section className="sf-auth-card">
        <div className="sf-logo">S</div>
        <h1>SkillForge</h1>
        <h2>{role === "ADMIN" ? "Admin Portal" : "Learner Portal"}</h2>

        <p className="sf-subtitle">
          {mode === "login" ? "Sign in to continue" : "Create your account"}
        </p>

        <form onSubmit={handleSubmit}>
          {mode === "register" && (
            <input
              type="text"
              placeholder="Full name"
              value={fullName}
              onChange={event => setFullName(event.target.value)}
              required
            />
          )}

          <input
            type="email"
            placeholder="Email address"
            value={email}
            onChange={event => setEmail(event.target.value)}
            required
          />

          <input
            type="password"
            placeholder="Password (minimum 8 characters)"
            value={password}
            onChange={event => setPassword(event.target.value)}
            minLength={8}
            required
          />

          <button
            type="submit"
            className="sf-primary"
            disabled={loading}
          >
            {loading
              ? "Please wait..."
              : mode === "login"
                ? "Sign In"
                : "Create Account"}
          </button>
        </form>

        {message && (
          <p className="sf-message" role="alert">
            {message}
          </p>
        )}

        {role === "LEARNER" && (
          <p className="sf-switch">
            {mode === "login" ? "New learner? " : "Already registered? "}
            <button
              type="button"
              className="sf-text-button"
              onClick={() => {
                setMode(mode === "login" ? "register" : "login");
                setMessage("");
              }}
            >
              {mode === "login" ? "Create Account" : "Sign In"}
            </button>
          </p>
        )}

        <button
          type="button"
          className="sf-text-button"
          onClick={goBack}
        >
          ← Change role
        </button>
      </section>
    </main>
  );
}