import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Admin.css';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

function Admin({ user, onLogout }) {
  const [theme, setTheme] = useState('');
  const [subTopics, setSubTopics] = useState([]);
  const [newSubTopic, setNewSubTopic] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [editName, setEditName] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    if (!user.isAdmin) {
      navigate('/');
      return;
    }
    fetchTheme();
    fetchSubTopics();
  }, []);

  const fetchTheme = async () => {
    try {
      const response = await fetch(`${API_URL}/api/theme`);
      const data = await response.json();
      setTheme(data.name || '');
    } catch (err) {
      console.error('Failed to fetch theme');
    }
  };

  const fetchSubTopics = async () => {
    try {
      const response = await fetch(`${API_URL}/api/subtopics`);
      const data = await response.json();
      setSubTopics(data);
    } catch (err) {
      console.error('Failed to fetch subtopics');
    }
  };

  const handleThemeUpdate = async () => {
    try {
      await fetch(`${API_URL}/api/theme`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${user.token}`
        },
        body: JSON.stringify({ name: theme })
      });
    } catch (err) {
      console.error('Failed to update theme');
    }
  };

  const handleCreateSubTopic = async (e) => {
    e.preventDefault();
    if (!newSubTopic.trim()) return;

    try {
      await fetch(`${API_URL}/api/subtopics`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${user.token}`
        },
        body: JSON.stringify({ name: newSubTopic })
      });
      setNewSubTopic('');
      fetchSubTopics();
    } catch (err) {
      console.error('Failed to create subtopic');
    }
  };

  const handleEdit = (subTopic) => {
    setEditingId(subTopic.id);
    setEditName(subTopic.name);
  };

  const handleSaveEdit = async (id) => {
    try {
      await fetch(`${API_URL}/api/subtopics/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${user.token}`
        },
        body: JSON.stringify({ name: editName })
      });
      setEditingId(null);
      fetchSubTopics();
    } catch (err) {
      console.error('Failed to update subtopic');
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this sub-topic and all its ideas?')) return;

    try {
      await fetch(`${API_URL}/api/subtopics/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${user.token}` }
      });
      fetchSubTopics();
    } catch (err) {
      console.error('Failed to delete subtopic');
    }
  };

  return (
    <div className="admin">
      <header className="admin-header">
        <h1>Admin Panel</h1>
        <div className="header-right">
          <button onClick={() => navigate('/dashboard')}>Dashboard</button>
          <button onClick={onLogout}>Logout</button>
        </div>
      </header>

      <div className="admin-content">
        <section className="admin-section">
          <h2>Main Theme</h2>
          <div className="theme-input">
            <input
              type="text"
              value={theme}
              onChange={(e) => setTheme(e.target.value)}
              placeholder="Enter main theme"
            />
            <button onClick={handleThemeUpdate}>Update Theme</button>
          </div>
        </section>

        <section className="admin-section">
          <h2>Sub-Topics</h2>
          <form onSubmit={handleCreateSubTopic} className="add-subtopic">
            <input
              type="text"
              value={newSubTopic}
              onChange={(e) => setNewSubTopic(e.target.value)}
              placeholder="New sub-topic name"
            />
            <button type="submit">Create</button>
          </form>

          <table className="subtopic-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Card Count</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {subTopics.map(st => (
                <tr key={st.id}>
                  <td>
                    {editingId === st.id ? (
                      <input
                        type="text"
                        value={editName}
                        onChange={(e) => setEditName(e.target.value)}
                      />
                    ) : (
                      st.name
                    )}
                  </td>
                  <td>{st.cardCount}</td>
                  <td>
                    {editingId === st.id ? (
                      <button onClick={() => handleSaveEdit(st.id)}>Save</button>
                    ) : (
                      <>
                        <button onClick={() => handleEdit(st)}>Edit</button>
                        <button className="delete-btn" onClick={() => handleDelete(st.id)}>Delete</button>
                      </>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      </div>
    </div>
  );
}

export default Admin;
