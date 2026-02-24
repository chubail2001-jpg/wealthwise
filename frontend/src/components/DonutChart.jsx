import { useEffect, useRef } from 'react';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

const fmt    = n => '$' + Number(n).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const LABELS = ['Income', 'Expense', 'Saving', 'Investment'];
const COLORS = ['#4ecb8d', '#e05c6b', '#5b9cf6', '#b57bee'];

export default function DonutChart({ income, expense, saving, investment }) {
  const canvasRef = useRef(null);
  const chartRef  = useRef(null);

  useEffect(() => {
    if (chartRef.current) chartRef.current.destroy();
    chartRef.current = new Chart(canvasRef.current.getContext('2d'), {
      type: 'doughnut',
      data: {
        labels: LABELS,
        datasets: [{ data: [income, expense, saving, investment], backgroundColor: COLORS, borderWidth: 0, hoverOffset: 8 }],
      },
      options: {
        responsive: false,
        cutout: '68%',
        plugins: {
          legend: { display: false },
          tooltip: { callbacks: { label: ctx => ` ${ctx.label}: ${fmt(ctx.raw)}` } },
        },
      },
    });
    return () => { if (chartRef.current) chartRef.current.destroy(); };
  }, [income, expense, saving, investment]);

  return (
    <>
      <canvas ref={canvasRef} width={220} height={220} />
      <div className="donut-legend">
        {LABELS.map((l, i) => (
          <div key={l} className="legend-item">
            <div className="legend-dot" style={{ background: COLORS[i] }} />
            <span>{l}</span>
          </div>
        ))}
      </div>
    </>
  );
}
