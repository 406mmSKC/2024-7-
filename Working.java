package origin_daoh;
import redis.clients.jedis.*;
public class Working extends Thread{
	private static boolean working=true;
	private static boolean over;
	
	@Override 
	public void run() {
		// TODO Auto-generated method stub
		Jedis jedis=RedisWR.getJedis();
		try {
			while(true) {
				working=jedis.get("Working").equals("1");
				over=jedis.exists("Over");
				System.out.println("workState: "+working+" isOver: "+over);
				if(!working&&over){
					System.exit(1);
				}
				Thread.sleep(2000);
			}
			
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
		}
	}
	
	public static boolean getWorking() {
		return working;
	}
	
	public static boolean getOver() {
		return over;
	}
}
