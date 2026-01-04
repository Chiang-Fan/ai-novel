import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';

const CharactersView = () => {
  const { id: novelId } = useParams();
  const [characters, setCharacters] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchCharacters();
  }, [novelId]);

  const fetchCharacters = async () => {
    try {
      const response = await axios.get(`/api/v1/novels/${novelId}/characters`);
      setCharacters(response.data);
      setLoading(false);
    } catch (error) {
      console.error('获取角色列表失败:', error);
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loading">加载中...</div>;
  }

  return (
    <div className="characters-view">
      <h3>角色面板</h3>
      <div className="characters-table-container">
        <table className="characters-table">
          <thead>
            <tr>
              <th>姓名</th>
              <th>核心信念</th>
              <th>当前阶段</th>
              <th>最后出现</th>
            </tr>
          </thead>
          <tbody>
            {characters.map((character) => (
              <tr key={character.id}>
                <td>{character.name}</td>
                <td>{character.coreBelief}</td>
                <td>{character.currentArcStage}</td>
                <td>{character.lastUpdatedChapter ? `第${character.lastUpdatedChapter}章` : '未出现'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {characters.length === 0 && (
        <div className="no-data">暂无角色信息</div>
      )}
    </div>
  );
};

export default CharactersView;