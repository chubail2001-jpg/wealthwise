export default function StatCard({ label, value, type, tag }) {
  return (
    <div className={`stat-card stat-${type}`}>
      <div className="stat-label">{label}</div>
      <div className="stat-value">{value}</div>
      <div className="stat-tag">{tag}</div>
    </div>
  );
}
