import numpy as np
stats = np.load('C:/Users/joelc/stage/greta_20240917_demo_v2_Lucie/greta/bin/Common/Data/MODIFFVQ/model/norm_stats.npy', allow_pickle=True).item()
print('min_x:', stats['min_x'])
print('max_x:', stats['max_x'])