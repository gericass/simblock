import jsonlines
from statistics import mean, median,variance,stdev


def print_sys(sys_times):
    sys_avg = mean(sys_times)
    sys_median = median(sys_times)
    sys_variance = variance(sys_times)
    sys_stdev = stdev(sys_times)
    print("実行時間")
    print('平均: {0:.2f}'.format(sys_avg))
    print('中央値: {0:.2f}'.format(sys_median))
    print('分散: {0:.2f}'.format(sys_variance))
    print('標準偏差: {0:.2f}'.format(sys_stdev))

def print_timestamp(timestamps):
    ts_avg = mean(timestamps)
    ts_median = median(timestamps)
    ts_variance = variance(timestamps)
    ts_stdev = stdev(timestamps)
    print("Timestamp")
    print('平均: {0:.2f}'.format(ts_avg))
    print('中央値: {0:.2f}'.format(ts_median))
    print('分散: {0:.2f}'.format(ts_variance))
    print('標準偏差: {0:.2f}'.format(ts_stdev))

if __name__ == '__main__':
    sys_times = []
    timestamps = []
    with jsonlines.open('./simulator/src/dist/output/time.json') as reader:
     for obj in reader:
         sys_times.append(obj["system_time"])
         timestamps.append(obj["timestamp"])
    print_sys(sys_times)
    print_timestamp(timestamps)

        