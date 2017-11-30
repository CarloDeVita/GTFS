/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demo;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import static net.sf.dynamicreports.report.builder.DynamicReports.cht;
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import net.sf.dynamicreports.report.builder.chart.XyLineChartBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.SubreportBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.exception.DRException;

/**
 *
 * @author cdevi
 */
public class Report {

    public void showGraphic(Statistic stat) throws DRException {
        
        SubreportBuilder freqTab = cmp.subreport(freqsTab(stat));
        SubreportBuilder namesP = cmp.subreport(pullmanTab(stat));
        JasperReportBuilder jrb = report()
                .summary(freqTab,namesP)
                .show(false);
    }

    private JasperReportBuilder freqsTab(Statistic stat) {
            /*Set styles*/
        Collection<Fascia> coll = new LinkedList<>();//create collection
        int[] freqs = stat.getFreqs();
        int start = stat.getStart();
        int end = stat.getEnd();
        for(int i=start;i<end;i++)
            coll.add(new Fascia(i,freqs[i]));
        
        /*Set styles*/
        StyleBuilder boldStyle = stl.style().bold();
        StyleBuilder columnStyle = stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
        StyleBuilder boldCenteredStyle = stl.style(boldStyle).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
        StyleBuilder columnTitleStyle  = stl.style(boldCenteredStyle)
		                                    .setBorder(stl.pen1Point())
		                                    .setBackgroundColor(Color.LIGHT_GRAY);
        
        TextColumnBuilder<Integer> hourColumn = col.column("Hour","hour",type.integerType());
        TextColumnBuilder<Integer> freqColumn = col.column("Frequency","freq",type.integerType());
        /*Create chart 3D*/
        XyLineChartBuilder graphic = cht.xyLineChart()
                                        .setTitle("Frequency chart")
                                        .setXValue(hourColumn)
                                        .setXAxisFormat(cht.axisFormat().setRangeMinValueExpression(start).setRangeMaxValueExpression(end).setLabel("Hours"))
                                        .setYAxisFormat(cht.axisFormat().setLabel("Number of bus"))
                                        .series(cht.xySerie(freqColumn));
                       
        JasperReportBuilder jrb = report()
                .setColumnTitleStyle(columnTitleStyle)
                .setColumnStyle(columnStyle)
                .highlightDetailEvenRows()
                .title(cmp.text("Report frequences"))
                .columns(hourColumn,freqColumn)
                .setDataSource(coll);
       
        return jrb;
    }

    private JasperReportBuilder pullmanTab(Statistic stat) {
        TextColumnBuilder<String> namesP = col.column("Pullman","name",type.stringType());
        //TextColumnBuilder<Integer> freqColumn = col.column("Frequency","freq",type.integerType());
        Set<StatPullman> pullmanStat = new HashSet<>();
        for(String s : stat.getPullman()){
            pullmanStat.add(new StatPullman(s));
        }
        JasperReportBuilder jrb = report()
                .columns(namesP)
                .setDataSource(pullmanStat);
        
        return jrb;
    }
    
     /*Classe fascia*/
    public static class Fascia{
        private int hour;
        private int freq;

        public Fascia(int hour, int freq){
            this.hour = hour;
            this.freq = freq;
        }
        
        public int getHour() {
            return hour;
        }

        public int getFreq() {
            return freq;
        }
    }
    
    public static class StatPullman{
        String name;

        public StatPullman(String name) {
            this.name = name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
        
    }
    
    /*Deve esserci una collection di classe FASCIA.*/
    public void showGraphic(int[] freqs, int start, Set<String> pullman) throws DRException {
        SubreportBuilder freqTab = cmp.subreport(freqsTab(freqs, start));
        SubreportBuilder namesP = cmp.subreport(pullmanTab(pullman));
        JasperReportBuilder jrb = report()
                .summary(freqTab,namesP)
                .show(false);
    }
    
    public JasperReportBuilder pullmanTab(Set<String> pullmans) throws DRException{
        TextColumnBuilder<String> namesP = col.column("Pullman","name",type.stringType());
        //TextColumnBuilder<Integer> freqColumn = col.column("Frequency","freq",type.integerType());
        Set<StatPullman> pullmanStat = new HashSet<>();
        for(String s : pullmans){
            pullmanStat.add(new StatPullman(s));
        }
        JasperReportBuilder jrb = report()
                .columns(namesP)
                .setDataSource(pullmanStat);
        return jrb;
    }
    
    public JasperReportBuilder freqsTab(int[] freqs, int start) throws DRException{
             /*Set styles*/
         Collection<Fascia> coll = new LinkedList<>();//create collection
        int len = freqs.length - start;
        for(int i=0;i<freqs.length;i++)
            coll.add(new Fascia(i+start,freqs[i]));
        
        /*Set styles*/
        StyleBuilder boldStyle = stl.style().bold();
        StyleBuilder columnStyle = stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
        StyleBuilder boldCenteredStyle = stl.style(boldStyle).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
        StyleBuilder columnTitleStyle  = stl.style(boldCenteredStyle)
		                                    .setBorder(stl.pen1Point())
		                                    .setBackgroundColor(Color.LIGHT_GRAY);
        
        TextColumnBuilder<Integer> hourColumn = col.column("Hour","hour",type.integerType());
        TextColumnBuilder<Integer> freqColumn = col.column("Frequency","freq",type.integerType());
        /*Create chart 3D*/
        XyLineChartBuilder graphic = cht.xyLineChart()
                                        .setTitle("Frequency chart")
                                        .setXValue(hourColumn)
                                        .setXAxisFormat(cht.axisFormat().setRangeMinValueExpression(start).setRangeMaxValueExpression(start+len).setLabel("Hours"))
                                        .setYAxisFormat(cht.axisFormat().setLabel("Number of bus"))
                                        .series(cht.xySerie(freqColumn));
                       
        JasperReportBuilder jrb = report()
                .setColumnTitleStyle(columnTitleStyle)
                .setColumnStyle(columnStyle)
                .highlightDetailEvenRows()
                .title(cmp.text("Report frequences"))
                .columns(hourColumn,freqColumn)
                .setDataSource(coll);
       
        return jrb;
    }
}
