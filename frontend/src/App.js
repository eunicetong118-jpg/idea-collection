import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Landing from './pages/Landing';
import Dashboard from './pages/Dashboard';
import Admin from './pages/Admin';

function App() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    const isAdmin = localStorage.getItem('isAdmin') === 'true';
    if (token && username) {
      setUser({ token, username, isAdmin });
    }
  }, []);

  const handleLogin = (authData) => {
    localStorage.setItem('token', authData.token);
    localStorage.setItem('username', authData.username);
    localStorage.setItem('isAdmin', authData.isAdmin);
    setUser(authData);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('isAdmin');
    setUser(null);
  };

  return (
    <Router>
      <Routes>
        <Route path="/" element={user ? <Navigate to="/dashboard" /> : <Landing onLogin={handleLogin} />} />
        <Route path="/dashboard" element={user ? <Dashboard user={user} onLogout={handleLogout} /> : <Navigate to="/" />} />
        <Route path="/admin" element={user && user.isAdmin ? <Admin user={user} onLogout={handleLogout} /> : <Navigate to="/" />} />
      </Routes>
    </Router>
  );
}

export default App;
