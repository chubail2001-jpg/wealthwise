import { useEffect, useRef } from 'react';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

const MONTHS = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];

export default function BreakdownChart({ transactions }) {
  const canvasRef = useRef(null);
  const chartRef  = useRef(null);

  useEffect(() => {
    const now   = new Date();
    const last6 = [];
    for (let i = 5; i >= 0; i--) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
      last6.push({ label: `${MONTHS[d.getMonth()]} '${String(d.getFullYear()).slice(2)}`, month: d.getMonth(), year: d.getFullYear() });
    }

    const getData = type =>
      last6.map(m =>
        transactions
          .filter(t => {
            const d = new Date(t.date);
            return t.type === type && d.getMonth() === m.month && d.getFullYear() === m.year;
          })
          .reduce((a, b) => a + Number(b.amount), 0)
      );

    if (chartRef.current) chartRef.current.destroy();
    chartRef.current = new Chart(canvasRef.current.getContext('2d'), {
      type: 'bar',
      data: {
        labels: last6.map(m => m.label),
        datasets: [
          { label: 'Income',     data: getData('INCOME'),     backgroundColor: 'rgba(78,203,141,0.7)',  borderRadius: 4 },
          { label: 'Expense',    data: getData('EXPENSE'),    backgroundColor: 'rgba(224,92,107,0.7)',  borderRadius: 4 },
          { label: 'Saving',     data: getData('SAVING'),     backgroundColor: 'rgba(91,156,246,0.7)',  borderRadius: 4 },
          { label: 'Investment', data: getData('INVESTMENT'), backgroundColor: 'rgba(181,123,238,0.7)', borderRadius: 4 },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        plugins: { legend: { labels: { color: '#6b6b8a', font: { family: 'DM Mono', size: 11 } } } },
        scales: {
          x: { ticks: { color: '#6b6b8a', font: { family: 'DM Mono', size: 11 } }, grid: { color: '#2a2a38' } },
          y: { ticks: { color: '#6b6b8a', font: { family: 'DM Mono', size: 11 }, callback: v => '$' + v.toLocaleString() }, grid: { color: '#2a2a38' } },
        },
      },
    });

    return () => { if (chartRef.current) chartRef.current.destroy(); };
  }, [transactions]);

  return <canvas ref={canvasRef} width={400} height={220} />;
}
