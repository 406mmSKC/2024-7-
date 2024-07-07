package origin_daoh;

public class SelectDestination {
	
	private static double[][] scores;
	private static int N;
	
	static {
		N=RedisWR.getLength();
		scores=new double[N][N];
	}
	
	// 获取目标点
	public static int[] getDes(int startX,int startY) throws InterruptedException {
	    int[][] mapArray=RedisWR.getMap("Bitmap");
	    
	    // 权重设置1
	    for(int i=1;i<N;i+=(N-3))
	    	for(int j=1;j<N-1;j++)
	    		scores[i][j]++;
	    for(int i=1;i<N;i+=(N-3))
	    	for(int j=1;j<N-1;j++)
	    		scores[j][i]++;
	    
	    // 权重设置2
	    for (int i = 0; i < N; i++) {
	        for (int j = 0; j < N; j++) {
	        	//未探索的区域
	            if (mapArray[i][j] == 0) {
	            	//根据距离设置权重
	            	double distance = Math.abs(startX-i)+Math.abs(startY-j);
	            	// 最近处权重高
	            	if(distance<5)
	            		scores[i][j]+=2;
	            	// 越远权重越高
	                scores[i][j] += distance/(N/2);
	                // 根据周围未探索区域密度设置权重
	                int count=0;
	                for (int dx = -1; dx <= 1; dx++) {
	                    for (int dy = -1; dy <= 1; dy++) {
	                        int newX = i + dx;
	                        int newY = j + dy;
	                        if (newX >= 0 && newX < N && newY >= 0 && newY < N && mapArray[newX][newY] == 0) {
	                            count++;
	                        }
	                    }
	                }
	                scores[i][j]+= (count==9)?1:0;
	            }
	        }
	    }
	    double maxScore = 0;
	    int[] point= {0,0};
	    for (int i = 0; i < N; i++) {
	        for (int j = 0; j < N; j++) {
	            if (mapArray[i][j] == 0 && scores[i][j] > maxScore) {
	                maxScore = scores[i][j];
	                point[0]=i;
	                point[1]=j;
	            }
	        }
	    }
	    // 目标位置附近权重置零（避免重复选择）
	    for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int newX = point[0] + dx;
                int newY = point[1] + dy;
                if (newX >= 0 && newX < N && newY >= 0 && newY < N ) {
                    scores[newX][newY]=0;
                }
            }
        }
	    return point;
	}
}
