package origin_daoh;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DynamicLineChartExample extends JFrame {

    private List<Point> points = new ArrayList<>();
    private int pointIndex = 0;

    public DynamicLineChartExample() {
        setTitle("Total Path Building Time");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Timer timer = new Timer(1000, e -> {
            System.out.println(points.size());
            points.add(new Point(pointIndex++, (int) (Main.duration/1000000)));
            System.out.println(Main.duration/1000000);
            repaint(); // 通知框架需要重绘
        });
        timer.start();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        FontMetrics fm = g.getFontMetrics();
        int labelHeight = fm.getHeight();

        // 绘制Y轴
        g.setColor(Color.BLACK);
        g.drawLine(50, 50, 50, getHeight() - 50);

        // 添加Y轴刻度和标签
        int yAxisTickStart = 50; // Y轴起点
        int yAxisTickEnd = 50 - 5; // 刻度线长度
        int yAxisInterval = 20; // 刻度间隔
        for (int i = 0; i <= 100; i += yAxisInterval) { // 假设Y轴最大值为100
            g.drawLine(45, yAxisTickStart + i * (getHeight() - 100) / 100, yAxisTickEnd, yAxisTickStart + i * (getHeight() - 100) / 100); // 绘制刻度线
            String label = String.valueOf(i);
            int labelWidth = fm.stringWidth(label);
            g.drawString(label, 10, yAxisTickStart + i * (getHeight() - 100) / 100 + labelHeight / 2 - 2); // 绘制刻度标签
        }
        // 绘制X轴
        g.drawLine(50, getHeight() - 50, getWidth() - 50, getHeight() - 50);

        // 添加X轴刻度和标签（简化示例，未考虑数据对应的实际刻度值）
        int xAxisTickStart = getHeight() - 50;
        int xAxisTickEnd = xAxisTickStart + 5;
        int xAxisInterval = (getWidth() - 100) / 5; // 假设有5个刻度
        for (int i = 0; i <= 5; i++) {
            g.drawLine(50 + i * xAxisInterval, xAxisTickStart, 50 + i * xAxisInterval, xAxisTickEnd);
            String label = String.valueOf(i * 20); // 示例标签，实际情况应根据数据调整
            g.drawString(label, 50 + i * xAxisInterval - fm.stringWidth(label) / 2, getHeight() - 40); // 居中显示标签
        }

        g.setColor(Color.BLUE);
        for (int i = 1; i < points.size(); i++) {
            Point prev = points.get(i - 1);
            Point curr = points.get(i);
            g.drawLine(prev.x, prev.y, curr.x, curr.y);
           // g.drawLine(10, 10, 100, 100); // 测试绘制一条固定的直线

        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DynamicLineChartExample ex = new DynamicLineChartExample();
            ex.setVisible(true);
        });
    }
}
