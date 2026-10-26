// Chart.js Visualizations for FINSIGHT
document.addEventListener('DOMContentLoaded', () => {
    initCategoryPieChart();
    initIncomeVsExpenseChart();
    initSavingsTrendChart();
});

function initCategoryPieChart() {
    const ctx = document.getElementById('expenseCategoryChart');
    if (!ctx) return;

    fetch('/api/charts/expense-by-category')
        .then(response => response.json())
        .then(data => {
            if (!data.labels || data.labels.length === 0) {
                const parent = ctx.parentElement;
                parent.innerHTML = '<div style="display:flex;align-items:center;justify-content:center;height:100%;color:#64748b;font-size:0.9rem;">No expense data available for this month</div>';
                return;
            }

            new Chart(ctx, {
                type: 'doughnut',
                data: {
                    labels: data.labels,
                    datasets: [{
                        data: data.data,
                        backgroundColor: data.backgroundColor,
                        borderWidth: 2,
                        borderColor: '#ffffff',
                        hoverOffset: 6
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'right',
                            labels: {
                                boxWidth: 12,
                                font: {
                                    family: "'Inter', sans-serif",
                                    size: 11
                                },
                                padding: 12
                            }
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    const val = context.raw || 0;
                                    return ` ₹${parseFloat(val).toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;
                                }
                            }
                        }
                    },
                    cutout: '65%'
                }
            });
        })
        .catch(err => console.error('Error loading category chart:', err));
}

function initIncomeVsExpenseChart() {
    const ctx = document.getElementById('incomeVsExpenseChart');
    if (!ctx) return;

    fetch('/api/charts/income-vs-expense?months=12')
        .then(response => response.json())
        .then(data => {
            new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: data.labels,
                    datasets: [
                        {
                            label: 'Income',
                            data: data.incomeData,
                            backgroundColor: '#10b981',
                            borderRadius: 6,
                            barPercentage: 0.6,
                            categoryPercentage: 0.8
                        },
                        {
                            label: 'Expense',
                            data: data.expenseData,
                            backgroundColor: '#ef4444',
                            borderRadius: 6,
                            barPercentage: 0.6,
                            categoryPercentage: 0.8
                        }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        x: {
                            grid: { display: false },
                            ticks: { font: { family: "'Inter', sans-serif", size: 11 } }
                        },
                        y: {
                            beginAtZero: true,
                            grid: { color: '#f1f5f9' },
                            ticks: {
                                font: { family: "'Inter', sans-serif", size: 11 },
                                callback: value => '₹' + value.toLocaleString()
                            }
                        }
                    },
                    plugins: {
                        legend: {
                            position: 'top',
                            labels: {
                                boxWidth: 12,
                                font: { family: "'Inter', sans-serif", size: 12 }
                            }
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    return `${context.dataset.label}: ₹${parseFloat(context.raw).toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;
                                }
                            }
                        }
                    }
                }
            });
        })
        .catch(err => console.error('Error loading income vs expense chart:', err));
}

function initSavingsTrendChart() {
    const ctx = document.getElementById('savingsTrendChart');
    if (!ctx) return;

    fetch('/api/charts/savings-trend?months=12')
        .then(response => response.json())
        .then(data => {
            new Chart(ctx, {
                type: 'line',
                data: {
                    labels: data.labels,
                    datasets: [{
                        label: 'Net Savings',
                        data: data.savingsData,
                        borderColor: '#6366f1',
                        backgroundColor: 'rgba(99, 102, 241, 0.1)',
                        fill: true,
                        tension: 0.35,
                        pointBackgroundColor: '#4f46e5',
                        pointBorderColor: '#ffffff',
                        pointBorderWidth: 2,
                        pointRadius: 4,
                        pointHoverRadius: 6
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        x: {
                            grid: { display: false },
                            ticks: { font: { family: "'Inter', sans-serif", size: 11 } }
                        },
                        y: {
                            grid: { color: '#f1f5f9' },
                            ticks: {
                                font: { family: "'Inter', sans-serif", size: 11 },
                                callback: value => '₹' + value.toLocaleString()
                            }
                        }
                    },
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    return `Savings: ₹${parseFloat(context.raw).toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;
                                }
                            }
                        }
                    }
                }
            });
        })
        .catch(err => console.error('Error loading savings trend chart:', err));
}
