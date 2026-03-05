import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import IdeaCard from '../components/IdeaCard';
import AddIdeaModal from '../components/AddIdeaModal';
import './Dashboard.css';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

function Dashboard({ user, onLogout }) {
  const [theme, setTheme] = useState('');
  const [subTopics, setSubTopics] = useState([]);
  const [selectedSubTopic, setSelectedSubTopic] = useState(null);
  const [ideas, setIdeas] = useState([]);
  const [sortBy, setSortBy] = useState('default');
  const [showAddModal, setShowAddModal] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    fetchTheme();
    fetchSubTopics();
  }, []);

  useEffect(() => {
    if (selectedSubTopic) {
      fetchIdeas();
    }
  }, [selectedSubTopic, sortBy]);

  const fetchTheme = async () => {
    try {
      const response = await fetch(`${API_URL}/api/theme`);
      const data = await response.json();
      setTheme(data.name || 'Welcome to Idea Collection');
    } catch (err) {
      setTheme('Idea Collection');
    }
  };

  const fetchSubTopics = async () => {
    try {
      const response = await fetch(`${API_URL}/api/subtopics`);
      const data = await response.json();
      setSubTopics(data);
      if (data.length > 0 && !selectedSubTopic) {
        setSelectedSubTopic(data[0].id);
      }
    } catch (err) {
      console.error('Failed to fetch subtopics');
    }
  };

  const fetchIdeas = async () => {
    try {
      const response = await fetch(
        `${API_URL}/api/ideas?subTopicId=${selectedSubTopic}&sortBy=${sortBy}`,
        { headers: { 'Authorization': `Bearer ${user.token}` } }
      );
      const data = await response.json();
      setIdeas(data);
    } catch (err) {
      console.error('Failed to fetch ideas');
    }
  };

  const handleCreateIdea = async (title, description) => {
    try {
      const response = await fetch(`${API_URL}/api/ideas`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${user.token}`,
          'X-User-Id': user.token,
          'X-Username': user.username
        },
        body: JSON.stringify({ subTopicId: selectedSubTopic, title, description })
      });

      if (response.ok) {
        fetchIdeas();
        fetchSubTopics();
      }
    } catch (err) {
      console.error('Failed to create idea');
    }
  };

  const handleStatusUpdate = async (ideaId, stage, stageStatus) => {
    try {
      const response = await fetch(`${API_URL}/api/ideas/${ideaId}/status`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${user.token}`
        },
        body: JSON.stringify({ stage, stageStatus })
      });

      if (response.ok) {
        fetchIdeas();
      }
    } catch (err) {
      console.error('Failed to update status');
    }
  };

  const handleLike = async (ideaId) => {
    try {
      const response = await fetch(`${API_URL}/api/ideas/${ideaId}/like`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${user.token}`, 'X-User-Id': user.token }
      });

      if (response.ok) {
        fetchIdeas();
      }
    } catch (err) {
      console.error('Failed to toggle like');
    }
  };

  return (
    <div className="dashboard">
      <header className="header">
        <div className="header-left">
          <h1>{theme}</h1>
        </div>
        <div className="header-right">
          <span>{user.username}</span>
          <button onClick={onLogout}>Logout</button>
        </div>
      </header>

      <nav className="tabs">
        {subTopics.map(st => (
          <button
            key={st.id}
            className={selectedSubTopic === st.id ? 'active' : ''}
            onClick={() => setSelectedSubTopic(st.id)}
          >
            {st.name}
          </button>
        ))}
      </nav>

      <div className="sort-controls">
        <span>Sort by:</span>
        <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
          <option value="default">Default</option>
          <option value="recent">Most Recent</option>
          <option value="most_liked">Most Liked</option>
          <option value="most_discussed">Most Discussed</option>
        </select>
      </div>

      <div className="ideas-grid">
        {ideas.map(idea => (
          <IdeaCard
            key={idea.id}
            idea={idea}
            user={user}
            onStatusUpdate={handleStatusUpdate}
            onLike={handleLike}
            apiUrl={API_URL}
          />
        ))}
      </div>

      <button className="add-btn" onClick={() => setShowAddModal(true)}>+</button>

      {showAddModal && (
        <AddIdeaModal
          onClose={() => setShowAddModal(false)}
          onSubmit={handleCreateIdea}
        />
      )}
    </div>
  );
}

export default Dashboard;
