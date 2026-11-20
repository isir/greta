import pandas as pd
import matplotlib.pyplot as plt

# Load your CSV file (adjust the path as needed)
df = pd.read_csv('FinalCrossinterpolated.csv')

# Define the groups (modify if necessary)
group1 = ['AU12_r']       # e.g., "smile" group
group2 = ['AU20_r']              # e.g., "sad/frown" group
group3 = ['AU15_r']                   # e.g., "surprise/other" group
# If you need AU2 to also appear in group3, you can add it here:
# group3 = ['AU15', 'AU14', 'AU2']

# Option 1: Compute summed activations per group
df['Group1'] = df[group1].sum(axis=1)
df['Group2'] = df[group2].sum(axis=1)
df['Group3'] = df[group3].sum(axis=1)

# Option 2: Alternatively, you might use the average activation
# df['Group1'] = df[group1].mean(axis=1)
# df['Group2'] = df[group2].mean(axis=1)
# df['Group3'] = df[group3].mean(axis=1)

# Create a time axis. If you have a 'timestamp' column, use it; otherwise, use frame index.

x = df['timestamp']


plt.figure(figsize=(12, 6))
plt.plot(x, df['Group1'], label='Group1 (AU12, AU25, AU6, AU2)', linewidth=2)
plt.plot(x, df['Group2'], label='Group2 (AU10, AU9, AU4)', linewidth=2)
plt.plot(x, df['Group3'], label='Group3 (AU15, AU14)', linewidth=2)
plt.xlabel('Time (frames)' if 'timestamp' not in df.columns else 'Time (s)')
plt.ylabel('Activation Intensity (summed)')
plt.title('Facial AU Activation Over Time by Group')
plt.legend()
plt.grid(True)
plt.tight_layout()
plt.show()
