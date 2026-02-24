import { useEffect, useRef } from 'react';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

const fmt = n => '$' + Number(n).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

export default function ForecastChart({ historical, projected }) {
  const canvasRef = useRef(null);
  const chartRef  = useRef(null);

  useEffect(() => {
    if (!historical?.length && !projected?.length) return;

    const labels     = [...historical.map(h => h.label), ...projected.map(p => p.label)];
    const values     = [...historical.map(h => h.balance), ...projected.map(p => p.balance)];
    const splitIndex = historical.length - 1;

    if (chartRef.current) chartRef.current.destroy();
    chartRef.current = new Chart(canvasRef.current.getContext('2d'), {
      type: 'line',
      data: {
        labels,
        datasets: [{
          label: 'Balance',
          data: values,
          borderColor: '#c8a96e',
          backgroundColor: 'rgba(200,169,110,0.1)',
          pointBackgroundColor: (ctx) => ctx.dataIndex > splitIndex ? '#b57bee' : '#c8a96e',
          pointRadius: 5,
          tension: 0.4,
          fill: true,
          segment: {
            borderColor: ctx => ctx.p0DataIndex >= splitIndex ? '#b57bee' : '#c8a96e',
            borderDash:  ctx => ctx.p0DataIndex >= splitIndex ? [6, 3]    : [],
          },
        }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        plugins: {
          legend: { display: false },
          tooltip: { callbacks: { label: ctx => ' Balance: ' + fmt(ctx.raw) } },
        },
        scales: {
          x: { ticks: { color: '#6b6b8a', font: { family: 'DM Mono', size: 11 } }, grid: { color: '#2a2a38' } },
          y: { ticks: { color: '#6b6b8a', font: { family: 'DM Mono', size: 11 }, callback: v => '$' + v.toLocaleString() }, grid: { color: '#2a2a38' } },
        },
      },
    });

    return () => { if (chartRef.current) chartRef.current.destroy(); };
  }, [historical, projected]);

  return <canvas ref={canvasRef} width={700} height={260} />;
}
