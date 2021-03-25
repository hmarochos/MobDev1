package ua.kpi.comsys.iv8218.mobdev1.ui.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.kpi.comsys.iv8218.mobdev1.R;

public class DashboardFragment extends Fragment {

    private LineChart graphic;
    private PieChart diagram;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        graphic = root.findViewById(R.id.chart_line);
        diagram = root.findViewById(R.id.chart_pie);

        List<Entry> data_graphic = new ArrayList<>();
        List<Entry> data_x_axis = new ArrayList<>();
        List<Entry> data_y_axis = new ArrayList<>();

        addGraphics(graphic, data_graphic, data_x_axis, data_y_axis);
        makeGraphic(root, graphic, data_graphic, data_x_axis, data_y_axis);
        makeDiagram(root, diagram);

        ToggleButton toggleButton = root.findViewById(R.id.toggle_graphic);

        toggleButton.setOnClickListener(v -> {
            if (toggleButton.isChecked()){
                graphic.setVisibility(View.GONE);
                diagram.setVisibility(View.VISIBLE);
            }
            else {
                diagram.setVisibility(View.GONE);
                graphic.setVisibility(View.VISIBLE);
            }
        });

        return root;
    }

    private void makeDiagram(View v, PieChart pieChart) {
        pieChart.getDescription().setEnabled(false);

        pieChart.getLegend().setCustom(new ArrayList<>());

        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        ArrayList<Integer> typeAmountMap = new ArrayList<>();
        typeAmountMap.add(35);
        typeAmountMap.add(40);
        typeAmountMap.add(25);

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.GREEN);
        colors.add(Color.YELLOW);
        colors.add(Color.RED);

        for(int i=0; i<typeAmountMap.size(); i++){
            pieEntries.add(new PieEntry(typeAmountMap.get(i).floatValue(), ""));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setValueTextSize(0f);
        pieDataSet.setColors(colors);

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextSize(0f);
        pieData.setValueTextColor(Color.WHITE);

        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    private void makeGraphic(View v, LineChart lineChart,
                             List<Entry> data_graphic,
                             List<Entry> data_x_axis,
                             List<Entry> data_y_axis){
        SortedMap<Double, Double> graph_main = new TreeMap<>();
        SortedMap<Double, Double> graph_X = new TreeMap<>();
        SortedMap<Double, Double> graph_Y = new TreeMap<>();

        lineChart.getXAxis().setAxisMinimum(-5);
        lineChart.getAxisLeft().setAxisMinimum(-5);
        lineChart.getAxisRight().setAxisMinimum(-5);
        lineChart.getXAxis().setAxisMaximum(5);
        lineChart.getAxisLeft().setAxisMaximum(5);
        lineChart.getAxisRight().setAxisMaximum(1);

        lineChart.getAxisLeft().setCenterAxisLabels(true);

        for (double i = -5; i <= 5; i+=0.05) {
            drawGraph(lineChart, graph_X, data_x_axis, 1, i, 0);
        }
        for (double i = -5; i <= 5; i+=0.05) {
            drawGraph(lineChart, graph_main, data_graphic, 0, i, i*i);
        }
        for (double i = -0.1; i <= 0.1; i+=0.05) {
            drawGraph(lineChart, graph_Y, data_y_axis, 2, i,100*i);
        }
    }

    private void addGraphics(LineChart line,
                             List<Entry> chartData_main,
                             List<Entry> chartData_X,
                             List<Entry> chartData_Y){
        line.getDescription().setEnabled(false);

        line.getLegend().setCustom(new ArrayList<>());

        setAxisParams(line.getXAxis());
        setAxisParams(line.getAxisLeft());
        setAxisParams(line.getAxisRight());

        line.getXAxis().setAxisMinimum(1f);
        line.getXAxis().setAxisMaximum(5f);

        LineDataSet chartDataSet_main = new LineDataSet(chartData_main, "Function");
        chartDataSet_main.setColor(ContextCompat.getColor(getContext(), R.color.purple_500));

        LineDataSet chartDataSet_X = new LineDataSet(chartData_X, "X");
        chartDataSet_X.setColor(ContextCompat.getColor(getContext(), R.color.black));

        LineDataSet chartDataSet_Y = new LineDataSet(chartData_Y, "Y");
        chartDataSet_Y.setColor(ContextCompat.getColor(getContext(), R.color.black));

        setDataSetParams(chartDataSet_main, 1.5f, 5f, false);
        setDataSetParams(chartDataSet_X, 1.5f, 5f, false);
        setDataSetParams(chartDataSet_Y, 1.5f, 5f, false);

        List<ILineDataSet> charDataSets = new ArrayList<>();
        charDataSets.add(chartDataSet_main);
        charDataSets.add(chartDataSet_X);
        charDataSets.add(chartDataSet_Y);

        LineData lineData = new LineData(charDataSets);
        line.setData(lineData);
    }

    private void drawGraph(LineChart l, Map<Double, Double> m, List data,
                           int index, double key, double value){
        m.put(key, value);

        data.clear();
        for (double v: m.keySet()) {
            data.add(new Entry((float)v, m.get(v).floatValue()));
        }
        LineDataSet set = (LineDataSet)l.getData().getDataSetByIndex(index);
        set.setValues(data);
        set.notifyDataSetChanged();
        l.getData().notifyDataChanged();
        l.notifyDataSetChanged();
        l.invalidate();
    }

    private void setAxisParams(AxisBase axis){
        axis.setDrawLabels(false);
        axis.setDrawAxisLine(false);
        axis.setDrawGridLines(false);
    }

    private void setDataSetParams(LineDataSet dataSet,
                                  float lineWidth, float circleRadius, boolean drawCircle){
        dataSet.setLineWidth(lineWidth);
        dataSet.setCircleRadius(circleRadius);

        dataSet.setDrawCircleHole(true);

        dataSet.setFormLineWidth(1f);
        dataSet.setFormSize(15.f);

        dataSet.setValueTextSize(9f);
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(drawCircle);
    }
}