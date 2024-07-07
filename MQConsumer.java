package origin_daoh;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class MQConsumer {
    private String QUEUE="";
    private String HOST="localhost";
    private String NAME;
    private Queue<String> messageList=new LinkedList<>(); 
    // 工厂
    private ConnectionFactory factory;
    // 连接
    private Connection connection;
    // 通道
    private Channel channel;
 
    // 指定队列名
    public MQConsumer(String name, String queue) {
    	this.NAME=name;
    	this.QUEUE=queue;
    	 //创建连接工厂
        factory = new ConnectionFactory();
        //设置RabbitMQ相关信息
        factory.setHost(HOST);
		try {
			// 创建连接
			connection = factory.newConnection();
			 //创建一个通道
			channel = connection.createChannel();	 
			startWork();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    // 指定ip、端口、队列名
    public MQConsumer(String name,String host,String queue) {
    	this.NAME=name;
    	this.HOST=host;
    	this.QUEUE=queue;
   	 	//创建连接工厂
    	factory = new ConnectionFactory();
    	//设置RabbitMQ相关信息
    	factory.setHost(HOST);
		try {
			// 创建连接
			connection = factory.newConnection();
			 //创建一个通道
			channel = connection.createChannel();
			startWork();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void startWork() throws IOException {
    	channel.queueDeclare(QUEUE, false, false, false, null);
        //DefaultConsumer类实现了Consumer接口，通过传入一个频道，
        // 告诉服务器我们需要那个频道的消息，如果频道中有消息，就会执行回调函数handleDelivery
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                messageList.offer(message);
                System.out.println(NAME + " receive message '" + message + "'"+"    the queue length is:"+messageList.size());
            }
        };
        //自动回复队列应答 -- RabbitMQ中的消息确认机制
        channel.basicConsume(QUEUE, true, consumer);
    }
    
    public void endWork() {
        //关闭通道和连接
        try {
			channel.close();
			connection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	public Queue<String> getMessageList() {
//		System.out.println("messageList:"+messageList.size());
		return messageList;
	}

}
