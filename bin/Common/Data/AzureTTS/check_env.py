import os
import sys
import subprocess
import pprint as pp

basedir = os.path.dirname(__file__)
os.chdir(basedir)

result = subprocess.run('check_env.bat', capture_output=True).stdout.decode('utf8').split('\r\n')
result = ''.join(result)
# pp.pprint(result)
# print(result)

status = 'py310_azuretts' in result

print(int(status))
