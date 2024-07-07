package origin_daoh;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.*;

import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;


public class Main {
	// 连接到 Redis 服务器
	// 设置连接超时时间为5秒
	static int connectionTimeout = 5000; // 单位是毫秒
	// 设置读取超时时间为5秒
	static int soTimeout = 5000; // 单位是毫秒

	static long duration=0;
	// 创建JedisClientConfig实例，配置超时时间
	//DefaultJedisClientConfig.Builder clientConfigBuilder = JedisClientConfig.builder();
	//JedisClientConfig clientConfig = clientConfigBuilder.connectTimeout(connectionTimeout).socketTimeout(soTimeout).build();
	private static Jedis jedis = new Jedis("localhost",6379,connectionTimeout,soTimeout);

	// consumer监听的队列
	private static MQConsumer consumer;
	private static final String QUEUE="Car_task";
	
	static {
		jedis.auth("1234567");
		consumer = new MQConsumer("cars", "localhost", QUEUE);
	}
	
	public static void main(String[] args) throws InterruptedException {

		Working working=new Working();
		working.start();
		long car1Time=0;
		long car2Time=0;
		long car3Time=0;
		long car4Time=0;
		jedis.set("car1Time", "0");
		jedis.set("car2Time", "0");
		jedis.set("car3Time", "0");
		jedis.set("car4Time", "0");
		//失败的绘图，目前不知道怎么实时监测小车寻路速度并绘制相关图表，留待有缘人
		//DynamicLineChartExample lineChartExample = new DynamicLineChartExample();
		//lineChartExample.setVisible(true);
		while (true) {

			try {
				Thread.sleep(500);
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			while(!Working.getOver() && Working.getWorking()) {
				
				System.out.println("(main)queue length: " + consumer.getMessageList().size());

				//jedis.set("startCal","0");

				if (!consumer.getMessageList().isEmpty()) {

					String car = consumer.getMessageList().poll();
					
					if (needNavigation(car)) {
						
						System.out.println(car+" need a path,info has been sent to navigation");
						
						int[] point = analysis(car);
						System.out.println("point is clear");
						
						int[][] map = RedisWR.getMap("Bitmap");
						System.out.println("map has been got");
						long startTime = System.nanoTime();

						navigation(point, map, jedis, car);
						long endTime = System.nanoTime();
						 duration = (endTime - startTime);  // divide by 1000000 to get milliseconds.
						System.out.println("Navigation time: " + duration/1000000 + " ms");
						if(car.equals("car1")){
							car1Time+=duration/1000000;
							System.out.println("car1Time is "+car1Time);
							jedis.set("car1Time",String.valueOf(car1Time));
						}
						if(car.equals("car2")){
							car2Time+=duration/1000000;
							System.out.println("car2Time is "+car2Time);
							jedis.set("car2Time",String.valueOf(car2Time));
						}
						if(car.equals("car3")){
							car3Time+=duration/1000000;
							System.out.println("car3Time is "+car3Time);
							jedis.set("car3Time",String.valueOf(car3Time));
						}
						if(car.equals("car4")){
							car4Time+=duration/1000000;
							System.out.println("car4Time is "+car4Time);
							jedis.set("car4Time",String.valueOf(car4Time));
						}
					}
				}
				
				try {
					if(consumer.getMessageList().size()>3){
						Thread.sleep(80);
					}
					else{
						Thread.sleep(500);
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
				
			}
			
			if(Working.getOver())
				break;
			
		}

		consumer.endWork();
		RedisWR.getJedis().close();
		working.stop();
		System.out.println("**************** NAVIGATION END ****************");
	}

	/***
	 * 
	 * 判断小车是否需要导航
	 * 
	 **/
	public static boolean needNavigation(String car) throws InterruptedException {
		// TODO Auto-generated method stub
		if (RedisWR.getCarState(car) == 0)
			return true;
		return false;
	}

	/***
	 * 
	 * 导航任务
	 * 
	 **/
	public static void navigation(int[] point, int[][] map, Jedis jedis2, String car) throws InterruptedException {
		// TODO Auto-generated method stub
		System.out.println("now "+car+" location: " + point[0] + "," + point[1] + "\n" + "target: " + point[2] + "," + point[3]);
		String al=jedis2.get("al");
		System.out.println("algorithm is "+al);
		List<String>route = null;
		if(al.equals("astar")){
			 route = AStar.findRoute(map, point[0], point[1], point[2], point[3]);
		}
		else if(al.equals("bfs")){
			route = BFS.findRouteBFS(map, point[0], point[1], point[2], point[3]);
		}
		else if(al.equals("dijkstra")){
			route = Dij.findRoute(map, point[0], point[1], point[2], point[3]);
		}

		else {
			System.out.println("no algorithm");
			System.exit(-2);
		}

		jedis.set("startCal","1");

		System.out.println("Path create finish");
		if (route != null) {
			String routeString = String.join("", route);
			System.out.println("Route: " + routeString);
			RedisWR.setCar(car, 2);
			System.out.println(car + " state is set 2");
			jedis.hset("Pathlist", car, routeString);
			jedis.hset("Pathlist2", car, routeString);
		} else {
			System.out.println("No path found!");
		}
	}

	/***
	 * 
	 * 解析小车当前位置、设置目标位置
	 * 
	 **/
	public static int[] analysis(String car) throws InterruptedException {
		// TODO Auto-generated method stub
		RedisWR.setCar(car, 1);
		System.out.println(car + " state set 1");
		int[] point = new int[4];
		int[] temp1 = RedisWR.getCarXY(car);
		int[] temp2 = SelectDestination.getDes(temp1[0], temp1[1]);
		point[0] = temp1[0];
		point[1] = temp1[1];
		point[2] = temp2[0];
		point[3] = temp2[1];
		return point;
	}
}