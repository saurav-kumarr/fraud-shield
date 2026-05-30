import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { Shield, User, Mail, Lock } from "lucide-react";

const Register = () => {
  const navigate = useNavigate();
  const { register } = useAuth();

  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      await register(formData);
      navigate("/dashboard");
    } catch (err) {
      setError(
        err.response?.data?.message || "Registration failed. Please try again.",
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="min-h-screen bg-gradient-to-br 
                    from-blue-500 to-purple-600 
                    flex items-center justify-center p-4"
    >
      <div
        className="bg-white rounded-2xl shadow-2xl 
                      p-8 w-full max-w-md"
      >
        <div className="flex flex-col items-center mb-6">
          <div className="bg-blue-100 p-4 rounded-full mb-4">
            <Shield className="w-10 h-10 text-blue-600" />
          </div>
          <h1 className="text-3xl font-bold text-gray-800">Create Account</h1>
          <p className="text-gray-500 mt-2">Join Fraud Shield today</p>
        </div>

        {error && (
          <div
            className="bg-red-50 border border-red-200 
                          text-red-700 px-4 py-3 rounded-lg 
                          mb-4"
          >
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label
                className="block text-sm font-medium 
                                text-gray-700 mb-2"
              >
                First Name
              </label>
              <div className="relative">
                <User
                  className="absolute left-3 top-3.5 
                                 w-5 h-5 text-gray-400"
                />
                <input
                  type="text"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleChange}
                  required
                  placeholder="John"
                  className="w-full pl-10 pr-3 py-3 border 
                             border-gray-300 rounded-lg 
                             focus:ring-2 
                             focus:ring-blue-500"
                />
              </div>
            </div>

            <div>
              <label
                className="block text-sm font-medium 
                                text-gray-700 mb-2"
              >
                Last Name
              </label>
              <input
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleChange}
                required
                placeholder="Doe"
                className="w-full px-3 py-3 border 
                           border-gray-300 rounded-lg 
                           focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          <div>
            <label
              className="block text-sm font-medium 
                              text-gray-700 mb-2"
            >
              Email
            </label>
            <div className="relative">
              <Mail
                className="absolute left-3 top-3.5 
                               w-5 h-5 text-gray-400"
              />
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                required
                placeholder="you@example.com"
                className="w-full pl-10 pr-4 py-3 border 
                           border-gray-300 rounded-lg 
                           focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          <div>
            <label
              className="block text-sm font-medium 
                              text-gray-700 mb-2"
            >
              Password
            </label>
            <div className="relative">
              <Lock
                className="absolute left-3 top-3.5 
                               w-5 h-5 text-gray-400"
              />
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                required
                minLength={6}
                placeholder="••••••••"
                className="w-full pl-10 pr-4 py-3 border 
                           border-gray-300 rounded-lg 
                           focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 text-white py-3 
                       rounded-lg font-semibold 
                       hover:bg-blue-700 transition 
                       disabled:bg-gray-400"
          >
            {loading ? "Creating account..." : "Create Account"}
          </button>
        </form>

        <p className="text-center text-gray-600 mt-6">
          Already have an account?{" "}
          <Link
            to="/login"
            className="text-blue-600 hover:underline 
                       font-semibold"
          >
            Sign In
          </Link>
        </p>
        <p className="text-center text-gray-500 text-sm mt-3">
          Are you a business?{" "}
          <Link
            to="/merchant-register"
            className="text-green-600 hover:underline 
               font-semibold"
          >
            Get API Key
          </Link>{" "}
          |{" "}
          <Link to="/api-docs" className="text-blue-600 hover:underline">
            View API Docs
          </Link>
        </p>
      </div>
    </div>
  );
};

export default Register;
