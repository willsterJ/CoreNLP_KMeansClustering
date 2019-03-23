/*
 * Class that implements JFreeChart to output a scatter plot of the data
 * data points will be colored according to their clusters
 */
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import java.util.*;

public class Visualization extends JFrame {

	public Visualization(String title, double[][] PCAMatrix, int k, List<String> clusterData) {
	    super(title);

	    // Create dataset
	    XYDataset dataset = createDataset(PCAMatrix, k, clusterData);

	    // Create chart
	    JFreeChart chart = ChartFactory.createScatterPlot(title, "PCA1", "PCA2", dataset);

	    // set background color
	    XYPlot plot = (XYPlot)chart.getPlot();
	    plot.setBackgroundPaint(new Color(255,255,255));
	   
	    // Create Panel
	    ChartPanel panel = new ChartPanel(chart);
	    setContentPane(panel);
	  }

	private XYDataset createDataset(double[][] PCAMatrix, int k, List<String> clusterData) {
		XYSeriesCollection dataset = new XYSeriesCollection();

		XYSeries series1 = new XYSeries("C1");
		XYSeries series2 = new XYSeries("C4");
		XYSeries series3 = new XYSeries("C7");
		
		for (int i=0; i<PCAMatrix.length; i++) {
			if (clusterData.get(i) == null)
				continue;
			if (clusterData.get(i).equals("C1"))
				series1.add(PCAMatrix[i][0], PCAMatrix[i][1]);
			else if (clusterData.get(i).equals("C4"))
				series2.add(PCAMatrix[i][0], PCAMatrix[i][1]);
			else if (clusterData.get(i).equals("C7"))
				series3.add(PCAMatrix[i][0], PCAMatrix[i][1]);
		}
		
		dataset.addSeries(series1);
		dataset.addSeries(series2);
		dataset.addSeries(series3);

		return dataset;
	}

}
