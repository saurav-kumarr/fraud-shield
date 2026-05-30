import { useState } from "react";
import { Link } from "react-router-dom";
import { merchantService } from "../services/merchantService";
import {
  Building,
  Mail,
  User,
  Globe,
  Copy,
  Check,
  ArrowLeft,
} from "lucide-react";
import toast from "react-hot-toast";

const MerchantRegister = () => {
  const [formData, setFormData] = useState({
    merchantName: "",
    merchantEmail: "",
    companyName: "",
    webhookUrl: "",
  });
  const [loading, setLoading] = useState(false);
  const [apiKeyResponse, setApiKeyResponse] = useState(null);
  const [copied, setCopied] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await merchantService.registerMerchant(formData);
      setApiKeyResponse(response);
      toast.success("API Key generated successfully!");
    } catch (error) {
      toast.error(error.response?.data?.message || "Registration failed");
    } finally {
      setLoading(false);
    }
  };

  const copyApiKey = () => {
    navigator.clipboard.writeText(apiKeyResponse.apiKey);
    setCopied(true);
    toast.success("API Key copied!");
    setTimeout(() => setCopied(false), 3000);
  };

  if (apiKeyResponse) {
    return (
      <div
        className="min-h-screen bg-gradient-to-br 
                      from-green-500 to-blue-600 
                      flex items-center justify-center p-4"
      >
        <div
          className="bg-white rounded-2xl shadow-2xl 
                        p-8 w-full max-w-2xl"
        >
          <div className="text-center mb-6">
            <div
              className="bg-green-100 w-20 h-20 rounded-full 
                            flex items-center justify-center 
                            mx-auto mb-4"
            >
              <Check className="w-12 h-12 text-green-600" />
            </div>
            <h1 className="text-3xl font-bold text-gray-800">
              Registration Successful!
            </h1>
            <p className="text-gray-600 mt-2">
              Save your API key securely. You won't be able to see it again.
            </p>
          </div>

          <div
            className="bg-yellow-50 border-l-4 
                          border-yellow-500 p-4 mb-6"
          >
            <p
              className="text-sm text-yellow-800 
                          font-semibold"
            >
              ⚠️ Important: Save this API key now. It cannot be retrieved later.
            </p>
          </div>

          <div className="bg-gray-50 rounded-xl p-6 mb-6">
            <p className="text-sm text-gray-500 mb-2">Your API Key</p>
            <div className="flex items-center space-x-2">
              <code
                className="flex-1 bg-white px-4 py-3 
                               rounded-lg font-mono text-sm 
                               break-all border border-gray-200"
              >
                {apiKeyResponse.apiKey}
              </code>
              <button
                onClick={copyApiKey}
                className={`p-3 rounded-lg transition ${
                  copied
                    ? "bg-green-500 text-white"
                    : "bg-blue-600 text-white hover:bg-blue-700"
                }`}
              >
                {copied ? (
                  <Check className="w-5 h-5" />
                ) : (
                  <Copy className="w-5 h-5" />
                )}
              </button>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4 mb-6">
            <div className="bg-blue-50 rounded-lg p-4">
              <p className="text-xs text-gray-500">Company</p>
              <p className="font-semibold text-gray-800">
                {apiKeyResponse.companyName}
              </p>
            </div>
            <div className="bg-purple-50 rounded-lg p-4">
              <p className="text-xs text-gray-500">Tier</p>
              <p className="font-semibold text-purple-700">
                {apiKeyResponse.tier}
              </p>
            </div>
          </div>

          <div className="bg-gray-50 rounded-lg p-4 mb-6">
            <p
              className="text-sm font-semibold text-gray-700 
                          mb-2"
            >
              How to use this API key:
            </p>
            <pre
              className="bg-gray-800 text-green-400 p-4 
                            rounded text-xs overflow-x-auto"
            >
              {`POST http://localhost:8080/api/v1/transactions/analyze

Headers:
  X-API-Key: ${apiKeyResponse.apiKey.substring(0, 20)}...
  Content-Type: application/json

Body:
{
  "merchantId": "your-merchant-id",
  "amount": 5000,
  "currency": "INR",
  "deviceId": "device-001",
  "ipAddress": "192.168.1.1",
  "location": "Delhi, India",
  "type": "PAYMENT"
}`}
            </pre>
          </div>

          <Link
            to="/login"
            className="block w-full bg-blue-600 text-white 
                       py-3 rounded-lg font-semibold 
                       hover:bg-blue-700 text-center"
          >
            Go to Login
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div
      className="min-h-screen bg-gradient-to-br 
                    from-green-500 to-blue-600 
                    flex items-center justify-center p-4"
    >
      <div
        className="bg-white rounded-2xl shadow-2xl 
                      p-8 w-full max-w-md"
      >
        <Link
          to="/login"
          className="flex items-center text-gray-500 
                     hover:text-gray-700 mb-4"
        >
          <ArrowLeft className="w-4 h-4 mr-1" />
          <span className="text-sm">Back to Login</span>
        </Link>

        <div className="flex flex-col items-center mb-6">
          <div className="bg-green-100 p-4 rounded-full mb-4">
            <Building className="w-10 h-10 text-green-600" />
          </div>
          <h1 className="text-3xl font-bold text-gray-800">
            Merchant Onboarding
          </h1>
          <p className="text-gray-500 mt-2 text-center">
            Get your API key for fraud detection integration
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label
              className="block text-sm font-medium 
                              text-gray-700 mb-2"
            >
              Company Name *
            </label>
            <div className="relative">
              <Building
                className="absolute left-3 top-3.5 
                                   w-5 h-5 text-gray-400"
              />
              <input
                type="text"
                name="companyName"
                value={formData.companyName}
                onChange={handleChange}
                required
                placeholder="Amazon India"
                className="w-full pl-10 pr-4 py-3 border 
                           border-gray-300 rounded-lg 
                           focus:ring-2 focus:ring-green-500"
              />
            </div>
          </div>

          <div>
            <label
              className="block text-sm font-medium 
                              text-gray-700 mb-2"
            >
              Contact Name *
            </label>
            <div className="relative">
              <User
                className="absolute left-3 top-3.5 
                               w-5 h-5 text-gray-400"
              />
              <input
                type="text"
                name="merchantName"
                value={formData.merchantName}
                onChange={handleChange}
                required
                placeholder="John Doe"
                className="w-full pl-10 pr-4 py-3 border 
                           border-gray-300 rounded-lg 
                           focus:ring-2 focus:ring-green-500"
              />
            </div>
          </div>

          <div>
            <label
              className="block text-sm font-medium 
                              text-gray-700 mb-2"
            >
              Email *
            </label>
            <div className="relative">
              <Mail
                className="absolute left-3 top-3.5 
                               w-5 h-5 text-gray-400"
              />
              <input
                type="email"
                name="merchantEmail"
                value={formData.merchantEmail}
                onChange={handleChange}
                required
                placeholder="john@amazon.com"
                className="w-full pl-10 pr-4 py-3 border 
                           border-gray-300 rounded-lg 
                           focus:ring-2 focus:ring-green-500"
              />
            </div>
          </div>

          <div>
            <label
              className="block text-sm font-medium 
                              text-gray-700 mb-2"
            >
              Webhook URL (Optional)
            </label>
            <div className="relative">
              <Globe
                className="absolute left-3 top-3.5 
                                w-5 h-5 text-gray-400"
              />
              <input
                type="url"
                name="webhookUrl"
                value={formData.webhookUrl}
                onChange={handleChange}
                placeholder="https://yourapp.com/webhook"
                className="w-full pl-10 pr-4 py-3 border 
                           border-gray-300 rounded-lg 
                           focus:ring-2 focus:ring-green-500"
              />
            </div>
            <p className="text-xs text-gray-500 mt-1">
              We'll send fraud verdicts to this URL
            </p>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-green-600 text-white py-3 
                       rounded-lg font-semibold 
                       hover:bg-green-700 transition 
                       disabled:bg-gray-400"
          >
            {loading ? "Generating API Key..." : "Get API Key"}
          </button>
        </form>

        <p className="text-center text-gray-600 mt-6">
          Have an account?{" "}
          <Link
            to="/login"
            className="text-green-600 hover:underline 
                       font-semibold"
          >
            Sign In
          </Link>
        </p>
        <p className="text-center text-gray-500 text-sm mt-3">
          <Link to="/api-docs" className="text-blue-600 hover:underline">
            Read API Documentation
          </Link>
        </p>
      </div>
    </div>
  );
};

export default MerchantRegister;
