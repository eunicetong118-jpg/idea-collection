import React, { useState, useEffect } from 'react';
import './IdeaCard.css';

function IdeaCard({ idea, user, onStatusUpdate, onLike, apiUrl }) {
  const [showComments, setShowComments] = useState(false);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');
  const [hasLiked, setHasLiked] = useState(false);

  useEffect(() => {
    setHasLiked(idea.likes?.includes(user.token));
  }, [idea.likes, user.token]);

  const handleCommentToggle = async () => {
    if (!showComments) {
      try {
        const response = await fetch(`${apiUrl}/api/ideas/${idea.id}/comments`);
        const data = await response.json();
        setComments(data);
      } catch (err) {
        console.error('Failed to fetch comments');
      }
    }
    setShowComments(!showComments);
  };

  const handleAddComment = async () => {
    if (!newComment.trim()) return;

    try {
      const response = await fetch(`${apiUrl}/api/ideas/${idea.id}/comments`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${user.token}`,
          'X-Username': user.username
        },
        body: JSON.stringify({ content: newComment })
      });

      if (response.ok) {
        const updatedComments = await fetch(`${apiUrl}/api/ideas/${idea.id}/comments`);
        setComments(await updatedComments.json());
        setNewComment('');
      }
    } catch (err) {
      console.error('Failed to add comment');
    }
  };

  const isDone = idea.stageStatus === 'Done';

  return (
    <div className={`idea-card ${isDone ? 'completed' : ''}`}>
      <div
        className="status-bar"
        onClick={() => user.isAdmin && onStatusUpdate(idea.id, idea.stage, prompt('Status:', idea.stageStatus))}
      >
        {idea.stage}: {idea.stageStatus}
      </div>

      <h3>{idea.title}</h3>
      <p className="description">{idea.description}</p>
      <p className="author">by {idea.author}</p>

      <div className="actions">
        <button
          className={`like-btn ${hasLiked ? 'liked' : ''}`}
          onClick={() => onLike(idea.id)}
        >
          ♥ {idea.likeCount}
        </button>
        <button className="comment-btn" onClick={handleCommentToggle}>
          💬 Comments
        </button>
      </div>

      {showComments && (
        <div className="comments-section">
          {comments.map(comment => (
            <div key={comment.id} className="comment">
              <strong>{comment.author}:</strong> {comment.content}
            </div>
          ))}
          <div className="comment-input">
            <input
              type="text"
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              placeholder="Add a comment..."
            />
            <button onClick={handleAddComment}>Submit</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default IdeaCard;
