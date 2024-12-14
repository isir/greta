import os
import sys
import subprocess
import pprint as pp

tgt_env_name = 'py310_mm'

basedir = os.path.dirname(__file__)
os.chdir(basedir)

result = subprocess.run('check_env.bat', capture_output=True).stdout.decode('utf8').split('\r\n')
# result = ''.join(result)
# pp.pprint(result)
# print(result)

for line in result:
    line = line.split(' ')[0]
    # print(line)
    if tgt_env_name == line:
        status = 1
    else:
        status = 0
    if status == 1:
        break

print(int(status))
