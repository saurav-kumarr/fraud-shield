import { useState } from 'react';
import { Link } from 'react-router-dom';
import { 
  Code, BookOpen, ArrowLeft, Copy, Check 
} from 'lucide-react';
import toast from 'react-hot-toast';

const ApiDocs = () => {
  const [copiedSection, setCopiedSection] = useState(null);

  const copyCode = (code, section) => {
    navigator.clipboard.writeText(code);
    setCopiedSection(section);
    toast.success('Code copied!');
    setTimeout(() => setCopiedSection(null), 2000);
  };

  const sections = [
    { id: 'overview', label: 'Overview' },
    { id: 'auth', label: 'Authentication' },
    { id: 'analyze', label: 'Analyze Transaction' },
    { id: 'examples', label: 'Code Examples' },
    { id: 'errors', label: 'Error Codes' },
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      
      <div className="bg-white shadow-md sticky top-0 z-10">
        <div className="max-w-6xl mx-auto px-4 py-4 
                        flex items-center justify-between">
          <Link 
            to="/login" 
            className="flex items-center text-gray-600 
                       hover:text-gray-800"
          >
            <ArrowLeft className="w-5 h-5 mr-2" />
            <span>Back</span>
          </Link>
          
          <h1 className="text-2xl font-bold text-gray-800 
                         flex items-center">
            <BookOpen className="w-6 h-6 mr-2 
                                  text-blue-600" />
            API Documentation
          </h1>

          <Link
            to="/merchant-register"
            className="bg-green-600 text-white px-4 py-2 
                       rounded-lg hover:bg-green-700 text-sm"
          >
            Get API Key
          </Link>
        </div>
      </div>

      <div className="max-w-6xl mx-auto px-4 py-8 
                      flex gap-8">

        <aside className="w-64 flex-shrink-0">
          <div className="bg-white rounded-xl shadow-md p-4 
                          sticky top-24">
            <h3 className="font-bold text-gray-800 mb-3">
              Contents
            </h3>
            <nav className="space-y-1">
              {sections.map(section => (
                <a
                  key={section.id}
                  href={`#${section.id}`}
                  className="block px-3 py-2 text-sm 
                             text-gray-600 hover:bg-blue-50 
                             hover:text-blue-600 rounded"
                >
                  {section.label}
                </a>
              ))}
            </nav>
          </div>
        </aside>

        <main className="flex-1 space-y-8">

          <section id="overview" 
                   className="bg-white rounded-xl 
                              shadow-md p-8">
            <h2 className="text-3xl font-bold text-gray-800 
                           mb-4">
              Fraud Shield API
            </h2>
            <p className="text-gray-600 mb-4 
                          leading-relaxed">
              Real-time fraud detection API for fintech 
              applications, banks, and e-commerce 
              platforms. Integrate fraud detection into 
              your payment flow with a single API call.
            </p>
            
            <div className="grid grid-cols-3 gap-4 mt-6">
              <div className="bg-blue-50 p-4 rounded-lg">
                <p className="text-2xl font-bold 
                              text-blue-600">
                  &lt;100ms
                </p>
                <p className="text-sm text-gray-600">
                  Response Time
                </p>
              </div>
              <div className="bg-green-50 p-4 rounded-lg">
                <p className="text-2xl font-bold 
                              text-green-600">
                  99.9%
                </p>
                <p className="text-sm text-gray-600">
                  Uptime
                </p>
              </div>
              <div className="bg-purple-50 p-4 rounded-lg">
                <p className="text-2xl font-bold 
                              text-purple-600">
                  4 Rules
                </p>
                <p className="text-sm text-gray-600">
                  Fraud Detection
                </p>
              </div>
            </div>
          </section>

          <section id="auth" 
                   className="bg-white rounded-xl 
                              shadow-md p-8">
            <h2 className="text-2xl font-bold text-gray-800 
                           mb-4">
              Authentication
            </h2>
            <p className="text-gray-600 mb-4">
              All API requests require an API key passed 
              in the <code className="bg-gray-100 px-2 
                                      py-1 rounded text-sm">
                X-API-Key
              </code> header.
            </p>

            <div className="bg-gray-900 rounded-lg p-4 
                            relative">
              <button
                onClick={() => copyCode(
                    'X-API-Key: fsk_your_api_key_here', 
                    'auth')}
                className="absolute top-2 right-2 
                           text-gray-400 hover:text-white"
              >
                {copiedSection === 'auth' ? 
                  <Check className="w-4 h-4" /> : 
                  <Copy className="w-4 h-4" />}
              </button>
              <pre className="text-green-400 text-sm">
{`X-API-Key: fsk_your_api_key_here`}
              </pre>
            </div>

            <div className="mt-4 bg-yellow-50 border-l-4 
                            border-yellow-500 p-4">
              <p className="text-sm text-yellow-800">
                <strong>⚠️ Important:</strong> Keep your 
                API key secure. Never expose it in 
                client-side code or public repositories.
              </p>
            </div>
          </section>

          <section id="analyze" 
                   className="bg-white rounded-xl 
                              shadow-md p-8">
            <h2 className="text-2xl font-bold text-gray-800 
                           mb-4">
              Analyze Transaction
            </h2>
            
            <div className="flex items-center space-x-2 
                            mb-4">
              <span className="bg-green-100 text-green-700 
                               px-3 py-1 rounded font-mono 
                               text-sm font-bold">
                POST
              </span>
              <code className="text-gray-700">
                /api/v1/transactions/analyze
              </code>
            </div>

            <h3 className="font-bold text-gray-800 mt-6 
                           mb-2">
              Request Body
            </h3>
            <div className="bg-gray-900 rounded-lg p-4 
                            relative">
              <button
                onClick={() => copyCode(
                  JSON.stringify({
                    merchantId: "merchant-001",
                    amount: 5000.00,
                    currency: "INR",
                    deviceId: "device-001",
                    ipAddress: "192.168.1.1",
                    location: "Delhi, India",
                    type: "PAYMENT"
                  }, null, 2), 'request')}
                className="absolute top-2 right-2 
                           text-gray-400 hover:text-white"
              >
                {copiedSection === 'request' ? 
                  <Check className="w-4 h-4" /> : 
                  <Copy className="w-4 h-4" />}
              </button>
              <pre className="text-green-400 text-sm 
                              overflow-x-auto">
{`{
  "merchantId": "merchant-001",
  "amount": 5000.00,
  "currency": "INR",
  "deviceId": "device-001",
  "ipAddress": "192.168.1.1",
  "location": "Delhi, India",
  "type": "PAYMENT"
}`}
              </pre>
            </div>

            <h3 className="font-bold text-gray-800 mt-6 
                           mb-2">
              Response (201 Created)
            </h3>
            <div className="bg-gray-900 rounded-lg p-4">
              <pre className="text-green-400 text-sm 
                              overflow-x-auto">
{`{
  "transactionId": "abc123-def456-...",
  "merchantId": "merchant-001",
  "amount": 5000.00,
  "currency": "INR",
  "status": "PENDING",
  "type": "PAYMENT",
  "createdAt": "2026-05-25T10:30:00"
}`}
              </pre>
            </div>
          </section>

          <section id="examples" 
                   className="bg-white rounded-xl 
                              shadow-md p-8">
            <h2 className="text-2xl font-bold text-gray-800 
                           mb-4">
              Code Examples
            </h2>

            <h3 className="font-bold text-gray-800 mt-4 
                           mb-2">
              cURL
            </h3>
            <div className="bg-gray-900 rounded-lg p-4 
                            relative">
              <button
                onClick={() => copyCode(
                  `curl -X POST http://localhost:8080/api/v1/transactions/analyze \\
  -H "X-API-Key: fsk_your_api_key" \\
  -H "Content-Type: application/json" \\
  -d '{"merchantId":"merchant-001","amount":5000,"currency":"INR","deviceId":"device-001","ipAddress":"192.168.1.1","location":"Delhi","type":"PAYMENT"}'`, 
                  'curl')}
                className="absolute top-2 right-2 
                           text-gray-400 hover:text-white"
              >
                {copiedSection === 'curl' ? 
                  <Check className="w-4 h-4" /> : 
                  <Copy className="w-4 h-4" />}
              </button>
              <pre className="text-green-400 text-sm 
                              overflow-x-auto">
{`curl -X POST http://localhost:8080/api/v1/transactions/analyze \\
  -H "X-API-Key: fsk_your_api_key" \\
  -H "Content-Type: application/json" \\
  -d '{
    "merchantId": "merchant-001",
    "amount": 5000,
    "currency": "INR",
    "deviceId": "device-001",
    "ipAddress": "192.168.1.1",
    "location": "Delhi",
    "type": "PAYMENT"
  }'`}
              </pre>
            </div>

            <h3 className="font-bold text-gray-800 mt-4 
                           mb-2">
              JavaScript (Node.js)
            </h3>
            <div className="bg-gray-900 rounded-lg p-4">
              <pre className="text-green-400 text-sm 
                              overflow-x-auto">
{`const axios = require('axios');

const response = await axios.post(
  'http://localhost:8080/api/v1/transactions/analyze',
  {
    merchantId: 'merchant-001',
    amount: 5000,
    currency: 'INR',
    deviceId: 'device-001',
    ipAddress: '192.168.1.1',
    location: 'Delhi',
    type: 'PAYMENT'
  },
  {
    headers: {
      'X-API-Key': 'fsk_your_api_key',
      'Content-Type': 'application/json'
    }
  }
);

console.log(response.data);`}
              </pre>
            </div>

            <h3 className="font-bold text-gray-800 mt-4 
                           mb-2">
              Python
            </h3>
            <div className="bg-gray-900 rounded-lg p-4">
              <pre className="text-green-400 text-sm 
                              overflow-x-auto">
{`import requests

response = requests.post(
    'http://localhost:8080/api/v1/transactions/analyze',
    headers={
        'X-API-Key': 'fsk_your_api_key',
        'Content-Type': 'application/json'
    },
    json={
        'merchantId': 'merchant-001',
        'amount': 5000,
        'currency': 'INR',
        'deviceId': 'device-001',
        'ipAddress': '192.168.1.1',
        'location': 'Delhi',
        'type': 'PAYMENT'
    }
)

print(response.json())`}
              </pre>
            </div>

            <h3 className="font-bold text-gray-800 mt-4 
                           mb-2">
              Java (Spring Boot)
            </h3>
            <div className="bg-gray-900 rounded-lg p-4">
              <pre className="text-green-400 text-sm 
                              overflow-x-auto">
{`HttpHeaders headers = new HttpHeaders();
headers.set("X-API-Key", "fsk_your_api_key");
headers.setContentType(MediaType.APPLICATION_JSON);

Map<String, Object> body = Map.of(
    "merchantId", "merchant-001",
    "amount", 5000,
    "currency", "INR",
    "deviceId", "device-001",
    "ipAddress", "192.168.1.1",
    "location", "Delhi",
    "type", "PAYMENT"
);

HttpEntity<Map<String, Object>> entity = 
    new HttpEntity<>(body, headers);

ResponseEntity<String> response = restTemplate.exchange(
    "http://localhost:8080/api/v1/transactions/analyze",
    HttpMethod.POST,
    entity,
    String.class
);`}
              </pre>
            </div>
          </section>

          <section id="errors" 
                   className="bg-white rounded-xl 
                              shadow-md p-8">
            <h2 className="text-2xl font-bold text-gray-800 
                           mb-4">
              Error Codes
            </h2>

            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-3 text-left 
                                   text-sm font-semibold 
                                   text-gray-700">
                      Status
                    </th>
                    <th className="px-4 py-3 text-left 
                                   text-sm font-semibold 
                                   text-gray-700">
                      Error
                    </th>
                    <th className="px-4 py-3 text-left 
                                   text-sm font-semibold 
                                   text-gray-700">
                      Description
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {[
                    { code: 400, error: 'Bad Request', 
                      desc: 'Invalid request data or ' +
                            'missing fields' },
                    { code: 401, error: 'Unauthorized', 
                      desc: 'Missing or invalid API key' },
                    { code: 404, error: 'Not Found', 
                      desc: 'Transaction not found' },
                    { code: 429, error: 'Too Many Requests', 
                      desc: 'Rate limit exceeded' },
                    { code: 500, error: 'Server Error', 
                      desc: 'Internal server error' },
                  ].map((row, idx) => (
                    <tr key={idx} className="hover:bg-gray-50">
                      <td className="px-4 py-3">
                        <span className={`font-mono font-bold 
                                          ${
                          row.code < 400 ? 'text-green-600' :
                          row.code < 500 ? 'text-orange-600' :
                          'text-red-600'
                        }`}>
                          {row.code}
                        </span>
                      </td>
                      <td className="px-4 py-3 font-semibold">
                        {row.error}
                      </td>
                      <td className="px-4 py-3 text-sm 
                                     text-gray-600">
                        {row.desc}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>

          <section className="bg-gradient-to-r from-blue-500 
                              to-purple-600 rounded-xl 
                              shadow-md p-8 text-white">
            <h2 className="text-2xl font-bold mb-2">
              Ready to integrate?
            </h2>
            <p className="mb-4 opacity-90">
              Get your API key in minutes and start 
              protecting your transactions.
            </p>
            <Link
              to="/merchant-register"
              className="inline-block bg-white text-blue-600 
                         px-6 py-3 rounded-lg font-semibold 
                         hover:bg-gray-100"
            >
              Get API Key →
            </Link>
          </section>
        </main>
      </div>
    </div>
  );
};

export default ApiDocs;