import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# Читаем CSV
df = pd.read_csv("tmp/results.csv")

# --- График 1: Serial vs Parallel (разные потоки) ---
plt.figure(figsize=(12,6))

serial_df = df[df['Type'] == 'Serial']
parallel_df = df[df['Type'] == 'Parallel']
threads = sorted(parallel_df['Threads'].unique())

plt.plot(serial_df['Vertices'], serial_df['Time_ms'], label='Serial BFS', marker='o', linewidth=2, color='black')

for t in threads:
    t_df = parallel_df[parallel_df['Threads'] == t]
    plt.plot(t_df['Vertices'], t_df['Time_ms'], marker='o', linestyle='--', label=f'Parallel BFS ({t} threads)')

plt.xlabel('Number of vertices')
plt.ylabel('Time (ms)')
plt.title('BFS Performance: Serial vs Parallel')
plt.legend()
plt.grid(True)
plt.savefig('bfs_time_vs_size_advanced.png')
plt.show()


# --- График 2: Влияние числа потоков на время Parallel BFS ---
plt.figure(figsize=(12,6))
max_v = 50000
parallel_maxv = parallel_df[parallel_df['Vertices'] == 50000]
plt.plot(parallel_maxv['Threads'], parallel_maxv['Time_ms'], marker='o', linestyle='-', color='red')
plt.xlabel('Number of threads')
plt.ylabel('Time (ms)')
plt.title(f'Parallel BFS Scaling with Number of Threads (V={max_v})')
plt.xticks(parallel_maxv['Threads'])
plt.grid(True)
plt.savefig('bfs_time_vs_threads_advanced.png')
plt.show()


