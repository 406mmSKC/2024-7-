package origin_daoh;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import redis.clients.jedis.*;

public class RedisWR {
	
	private static Jedis jedis=new Jedis("localhost",6379,5000,5000);
	static {
	//	jedis.auth("1234567");
	}

	private static int LEN=Integer.parseInt(jedis.get("Length")); 
	
	
	// 地图 读 锁
	private static String MAPREAD="readmap";
	private static int MAPREAD_NUM=0;

	// 地图 写 锁
	private static String MAPWRITE="writemap";
	
	// 小车 读 锁
	private static String CARSREAD="readcars";
	private static int CARSREAD_NUM=0;
	
	// 小车 写 锁
	private static String CARSWRITE="writecars";
	
	// 地图加读锁
	public static boolean readMapLock() throws InterruptedException {
		boolean shouldRetryForMapWrite = true;
		int retryCountForMapWrite = 0;
		final int MAX_RETRY_ATTEMPTS = 5;

		while (shouldRetryForMapWrite) {
			try {
				retryCountForMapWrite++;
				if (retryCountForMapWrite > MAX_RETRY_ATTEMPTS) {
					throw new Exception("Maximum retry attempts exceeded for map write access.");
				}

				while (jedis.exists(MAPWRITE)) {
					Thread.sleep(50);
				}

				shouldRetryForMapWrite = false; // 成功则停止重试
			} catch (InterruptedException e) {
				System.out.println("-----------------------------------------------");
				System.out.println("Interrupted while waiting for map write access.");
				Thread.currentThread().interrupt();
				System.out.println("-----------------------------------------------");
			} catch (Exception e) {
				System.out.println("-----------------------------------------------");
				System.out.println("-----------------------------------------------");
				System.out.println("-----------------------------------------------");
				System.out.println("Failed to access map write on attempt " + retryCountForMapWrite);
				e.printStackTrace();
				Thread.sleep(200);
				System.out.println("-----------------------------------------------");
				System.out.println("-----------------------------------------------");
				System.out.println("-----------------------------------------------");

				// 根据具体情况判断是否继续重试
			}
		}

// 重试循环结束后继续执行的代码

		Transaction tx = null;
		int lo=0;
    	try {
            tx = jedis.multi();
            tx.setnx(MAPREAD, "test");
            tx.pexpire(MAPREAD, 10000);            // 执行事务
            List<Object> result = tx.exec();
            if (result != null && result.size() == 2) {// 如果两个命令都执行成功了
                    lo = 1;
                    MAPREAD_NUM++;
            } else {
                    lo = 0;
              }
        } catch (Exception e) {
        	System.out.println("fail to add map read");
            if (tx != null) {// 如果出现异常，撤销事务
				int retryTXCount = 0;
				while (retryTXCount < 5) {
					try {
						tx.discard(); // 尝试执行discard操作
						break; // 如果成功，跳出循环
					} catch (Exception eTX) {
						retryTXCount++; // 记录重试次数
						if (retryTXCount >= 5) {
							System.err.println("Discard operation failed after 5 retries. No further attempts will be made.");
							eTX.printStackTrace(); // 打印异常信息，以便于调试
							break; // 达到最大重试次数，不再重试
						} else {
							System.err.println("Attempt " + retryTXCount + " to discard ADD MAP READ transaction failed. Retrying...");
							Thread.sleep(100);
						}
					}
				}

			}
            e.printStackTrace();
			System.out.println("i am in 97");
        }
		return lo==1;
	}
	
	// 地图解读锁
	public static boolean readMapUnLock() {
		try {
			if((--MAPREAD_NUM)==0) {
				jedis.del(MAPREAD);
			}
			return true;
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
			System.out.println("i am in 111");
			return false;
		}
	}
	
	// 地图加写锁
	public static boolean writeMAPLock(String name) throws InterruptedException {
		boolean shouldRetryForMap = true;
		int retryCountForMap = 0;
		final int MAX_RETRY_ATTEMPTS = 5;

		while (shouldRetryForMap) {
			try {
				retryCountForMap++;
				if (retryCountForMap > MAX_RETRY_ATTEMPTS) {
					throw new Exception("Maximum retry attempts exceeded for map access.");
				}

				while (jedis.exists(MAPREAD) || jedis.exists(MAPWRITE)) {
					Thread.sleep(50);
				}

				shouldRetryForMap = false; // 成功则停止重试
			} catch (InterruptedException e) {
				System.out.println("-----------------------------------------------");
				System.out.println("Interrupted while waiting for map access!Code at RWR-91.");
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				System.out.println("-----------------------------------------------");
				System.out.println("-----------------------------------------------");
				System.out.println("-----------------------------------------------");
				System.out.println("Failed to access map on attempt " + retryCountForMap);
				e.printStackTrace();
				Thread.sleep(200);
				System.out.println("-----------------------------------------------");
				System.out.println("-----------------------------------------------");
				System.out.println("-----------------------------------------------");

				// 根据异常情况判断是否应该继续重试
			}
		}

// 重试循环结束后继续执行的代码

		int lo = 0;
		Transaction tx = null;
    	try {
            tx = jedis.multi();
            tx.setnx(MAPWRITE, name);
            tx.pexpire(MAPWRITE, 10000);            // 执行事务
            List<Object> result = tx.exec();
            if (result != null && result.size() == 2) {// 如果两个命令都执行成功了
                if ((Long)result.get(0) == 1 && (Long)result.get(1) == 1) {
                    lo = 1;
                } else {
                    lo = 0;
                }
            }
        } catch (Exception e) {
        	System.out.println("fail to add map write");
            if (tx != null) {// 如果出现异常，撤销事务
				int retryTXCount = 0;
				while (retryTXCount < 5) {
					try {
						tx.discard(); // 尝试执行discard操作
						break; // 如果成功，跳出循环
					} catch (Exception eTX) {
						retryTXCount++; // 记录重试次数
						if (retryTXCount >= 5) {
							System.err.println("Discard operation failed after 5 retries. No further attempts will be made.");
							eTX.printStackTrace(); // 打印异常信息，以便于调试
							break; // 达到最大重试次数，不再重试
						} else {
							System.err.println("Attempt " + retryTXCount + " to discard  ADD MAP WRITE transaction failed. Retrying...");
							Thread.sleep(100);
						}
					}
				}

			}
            e.printStackTrace();
			System.out.println("i am in 174");
        }
		return lo==1;
	}
	
	// 地图解写锁
	public static boolean writeMAPUnLock(String name) {
		if(jedis.exists(MAPWRITE) && jedis.get(MAPWRITE).equals(name)) {
			jedis.del(MAPWRITE);
			return true;
		}else {
			System.out.println("fail to unlock map write");
			return false;
		}
	}		
	
	// 小车加读锁
	public static boolean readCarsLock() throws InterruptedException {
		boolean shouldRetryForRead = true;
		int retryCountForRead = 0;
		final int MAX_RETRY_ATTEMPTS = 5;

		while (shouldRetryForRead) {
			try {
				retryCountForRead++;
				if (retryCountForRead > MAX_RETRY_ATTEMPTS) {
					throw new Exception("Maximum retry attempts exceeded for reading.");
				}

				while (jedis.exists(CARSWRITE)) {
					Thread.sleep(50);
				}

				shouldRetryForRead = false; // 成功则停止重试
			} catch (InterruptedException e) {
				System.out.println("-----------------------------------------------");
				System.out.println("Interrupted while waiting to add car read.");
				System.out.println("-----------------------------------------------");
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				System.out.println("-----------------------------------------------");
				System.out.println("-----------------------------------------------");
				System.out.println("-----------------------------------------------");
				System.out.println("Failed to add car read on attempt " + retryCountForRead);
				e.printStackTrace();
				Thread.sleep(200);
				System.out.println("-----------------------------------------------");
				System.out.println("-----------------------------------------------");
				System.out.println("-----------------------------------------------");

				// 根据具体情况判断是否应该继续重试
			}
		}

// 重试循环结束后继续执行的代码

		Transaction tx = null;
			int lo = 0;

			try {
				tx = jedis.multi();
				tx.setnx(CARSREAD, "test");
				tx.pexpire(CARSREAD, 10000);            // 执行事务
				List<Object> result = tx.exec();
				if (result != null && result.size() == 2) {// 如果两个命令都执行成功了
					lo = 1;
					CARSREAD_NUM++;
				} else {
					lo = 0;
				}
			} catch (Exception e) {
				System.out.println("car is being read ,i can not read it!");
				if (tx != null ) {// 如果出现异常，撤销事务
					int retryTXCount = 0;
					while (retryTXCount < 5) {
						try {
							tx.discard(); // 尝试执行discard操作
							break; // 如果成功，跳出循环
						} catch (Exception eTX) {
							retryTXCount++; // 记录重试次数
							if (retryTXCount >= 5) {
								System.err.println("Discard operation failed after 5 retries. No further attempts will be made.");
								eTX.printStackTrace(); // 打印异常信息，以便于调试
								break; // 达到最大重试次数，不再重试
							} else {
								System.err.println("Attempt " + retryTXCount + " to discard ADD CAR READ transaction failed. Retrying...");
								Thread.sleep(100);
							}
						}
					}

				}
				e.printStackTrace();
				System.out.println("i am in 249");
			}
			return lo == 1;
		}


	// 小车解读锁
	public static boolean readCarsUnLock() {
		try {
			if((--CARSREAD_NUM)==0) {
				jedis.del(CARSREAD);
			}
			return true;
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage()+"i am in 263");
			return false;
		}
	}
	
	// 小车加写锁
	public static boolean writeCarsLock(String name) throws InterruptedException {
		boolean shouldRetry = true;
		int retryCount = 0;
		final int MAX_RETRY_ATTEMPTS = 5; // 定义最大重试次数

		while (shouldRetry) {
			try {
				retryCount++;
				// 检查是否达到最大重试次数
				if (retryCount > MAX_RETRY_ATTEMPTS) {
					throw new Exception("Maximum retry attempts exceeded.");
				}

				while (jedis.exists(CARSREAD) || jedis.exists(CARSWRITE)) {
					Thread.sleep(50);
				}

				// 如果成功执行到这里，说明没有异常，设置shouldRetry为false以跳出循环
				shouldRetry = false;
			} catch (InterruptedException e) {
				// 处理中断异常
				System.out.println("-----------------------------------------------");
				System.out.println("Interrupted while waiting.");
				System.out.println("-----------------------------------------------");
				Thread.currentThread().interrupt(); // 重新设置中断状态
			} catch (Exception e) {
				System.out.println("-----------------------------------------------");
				System.out.println("-----------------------------------------------");
				System.out.println("-----------------------------------------------");
				System.out.println("Failed to add car write on attempt " + retryCount);
				e.printStackTrace();
				Thread.sleep(200);
				System.out.println("-----------------------------------------------");
				System.out.println("-----------------------------------------------");
				System.out.println("-----------------------------------------------");

				// 根据异常情况判断是否继续重试，这里默认总是重试直到达到最大次数
			}
		}

// 循环结束后继续执行的代码

		int lo = 0;
		Transaction tx = null;
    	try {
            tx = jedis.multi();
            tx.setnx(CARSWRITE, name);
            tx.pexpire(CARSWRITE, 10000);            // 执行事务
            List<Object> result = tx.exec();
            if (result != null && result.size() == 2) {// 如果两个命令都执行成功了
                if ((Long)result.get(0) == 1 && (Long)result.get(1) == 1) {
                    lo = 1;
                } else {
                    lo = 0;
                }
            }
        } catch (Exception e) {
            System.out.println("fail to add car write");
            if (tx != null) {// 如果出现异常，撤销事务
				int retryTXCount = 0;
				while (retryTXCount < 5) {
					try {
						tx.discard(); // 尝试执行discard操作
						break; // 如果成功，跳出循环
					} catch (Exception eTX) {
						retryTXCount++; // 记录重试次数
						if (retryTXCount >= 5) {
							System.err.println("Discard operation failed after 5 retries. No further attempts will be made.");
							eTX.printStackTrace(); // 打印异常信息，以便于调试
							break; // 达到最大重试次数，不再重试
						} else {
							System.err.println("Attempt " + retryCount + " to discard ADD CAR WRITE transaction failed. Retrying...");
							Thread.sleep(100);
						}
					}
				}

			}
            e.printStackTrace();
			System.out.println("i am in 335");
        }
		return lo==1;
	}
	
	// 小车解写锁
	public static boolean writeCarsUnLock(String name) {
		if(jedis.exists(CARSWRITE) && jedis.get(CARSWRITE).equals(name)) {
			jedis.del(CARSWRITE);
			return true;
		}else {
			System.out.println("fail to unlock car write");
			return false;
		}
	}	
	 
	// 获取小车当前状态
    public static int getCarState(String car) throws InterruptedException {
    	int state=0;
		try{
    	if(readCarsLock()) {
    		String json = jedis.hget("Cars", car);
    		try {
    		    ObjectMapper mapper = new ObjectMapper();
    		    JsonNode car2JsonNode = mapper.readTree(json);
    		    state=((ObjectNode) car2JsonNode).get("state").asInt();
    		} catch (JsonProcessingException e) {
    		    //e.printStackTrace();
				//一般来说导航完成的时候这里就会报错
				System.out.println("Over");
				System.exit(1);
    		}finally {
    			readCarsUnLock();
    		}
    	}
		}
		catch (Exception ex){
			System.out.println("i am in 440");
		}
    	return state;
    }	
    
	// 修改小车状态
	public static void setCar(String car, int state) throws InterruptedException {
    	if(writeCarsLock(car)) {
    		String json=jedis.hget("Cars", car);
        	try {
        	    ObjectMapper mapper = new ObjectMapper();
        	    JsonNode car2JsonNode = mapper.readTree(json);
        	    ((ObjectNode) car2JsonNode).put("state", state);
        	    String updatedJson = mapper.writeValueAsString(car2JsonNode);
        	    jedis.hset("Cars", car,updatedJson);
        	} catch (JsonProcessingException e) {
        	    e.printStackTrace();
				System.out.println("i am in 383");
        	    System.out.println("fail to update car state");
        	}finally {
        		writeCarsUnLock(car);
        	}
    	}
    }
	
	// 读取小车当前坐标
	public static int[] getCarXY(String car) throws InterruptedException {
		if(readCarsLock()) {
			String str=jedis.hget("Cars", car);
			int[] point=new int[2];
			try {
				point[0]=new ObjectMapper().readTree(str).get("x").asInt();
				point[1]=new ObjectMapper().readTree(str).get("y").asInt();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("i am in 401");
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("i am in 404");
			}finally {
				readCarsUnLock();
			}
			return point;
		}
		System.out.println("fail to read car info");
		return null;
	}
	
	// 读地图（x，y）的值
    public static Boolean getMapBit(int x, int y, String name) throws InterruptedException {
        Boolean bit = null;
        if(readMapLock()) {
        	bit = jedis.getbit(name,x*LEN+y);
        	readMapUnLock();
        }
        return bit;
    }

    // 读取地图
    public static int[][] getMap(String mapName) throws InterruptedException {
    	if(readMapLock()) {
    		int[][] map=new int[LEN][LEN];
    		int n=0;
    		byte[] valueBytes = jedis.getrange(mapName.getBytes(StandardCharsets.UTF_8), 0, -1);    	
        	the:for (int i = 0; i < valueBytes.length; i++) {
        	    byte b = valueBytes[i];
        	    for (int j = 7; j >= 0; j--) {
        	        boolean bitValue = ((b >> j) & 1) == 1;
        	        map[n/LEN][n%LEN]=bitValue?1:0;
        	        n++;
        	        if(n==LEN*LEN) {
        	        	break the;
        	        }
        	    }
        	}
        	readMapUnLock();
        	return map;
    	}
    	System.out.println("fail to read map");
    	return null;
    }

	public static int getLength() {
		return LEN;
	}

	public static Jedis getJedis() {
		// TODO Auto-generated method stub
		return jedis;
	}
}
