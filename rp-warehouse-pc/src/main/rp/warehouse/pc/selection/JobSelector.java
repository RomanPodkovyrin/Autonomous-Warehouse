package rp.warehouse.pc.selection;

import org.apache.log4j.Logger;
import rp.warehouse.pc.data.Task;
import rp.warehouse.pc.input.Job;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class JobSelector {

    private final static Logger logger = Logger.getLogger(JobSelector.class);
    private ArrayList<Job> jobs;
    private boolean cancelled;
    private boolean predictedCancel;
    private float value;

    public JobSelector(ArrayList<Job> jobs, int cancelled, boolean predictedCancel, float value) {
        this.jobs = jobs;
        this.predictedCancel = predictedCancel;
        this.cancelled = cancelled == 1;
        this.value = value;
    }

    public void setPrediction(boolean prediction) {
        this.predictedCancel = prediction;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean c) {
        this.cancelled = c;
    }

    private float totalReward(Job j) {
        float total = 0;
        for (Task t : j.getItems()) {
            logger.trace("Calculating total reward of given job.");
            total = total + t.getItem().getReward() * t.getCount();
            logger.trace("Logic: item reward multiplied by item count.");
        }
        return 0;
    }


    public int totalItems(Job j) {
        int total = 0;
        for (Task t : j.getItems()) {
            logger.trace("Calculating count of items given job has.");
            total = total + t.getCount();
        }
        return total;
    }

    public void sortByReward() {
        logger.debug("Sorting jobs based on total reward.");
        jobs.sort((a, b) -> (int) totalReward(b) / b.numOfTasks() - (int) totalReward(a) / a.numOfTasks());
    }

    private void sortByReward(ArrayList<Job> j) {
        logger.debug("Sorting jobs based on total reward.");
        j.sort((a, b) -> {
            final float aReward = totalReward(a), bReward = totalReward(b);
            if (aReward == bReward) {
                return (int) (a.getItems().stream().mapToDouble(t -> t.count * t.item.getWeight()).sum() - b.getItems().stream().mapToDouble(t -> t.count * t.item.getWeight()).sum());
            } else {
                return (int) (bReward - aReward);
            }
        });
    }

    public List<Job> sortPredicted(String pfile) {
        BufferedReader reader;
        ArrayList<Job> validJobs = new ArrayList<>(); //An ArrayList for the jobs that won't be potentially cancelled.
        ArrayList<Job> cancelledJobs = new ArrayList<>(); //An ArrayList for the jobs that will be potentially cancelled.
        HashMap<String, Integer> predictions = new LinkedHashMap<>(); //Predictions from WEKA put into a HashMap of ids and values.

        try {
            reader = new BufferedReader(new FileReader(pfile));
            String line;
            logger.debug("Started reading from prediction file...");
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                data[1] = data[1].substring(0, 1);
                logger.debug("Putting results from WEKA into a HashMap.");
                predictions.put(data[0], Integer.parseInt(data[1]));
            }
            logger.debug("Splitting jobs into arrays called on cancellation...");
            int index = 0;
            for (Integer i : predictions.values()) {
                if (i == 0)
                    validJobs.add(jobs.get(index));
                else
                    cancelledJobs.add(jobs.get(index));
                index++;
            }
            logger.debug("Sorting both arrays based on total reward and concatenating");
            sortByReward(jobs);
            sortByReward(cancelledJobs);
            jobs.addAll(cancelledJobs);

            reader.close();

            validJobs.forEach((a) -> System.out.println(a.getItems()));
            return validJobs;

        } catch (FileNotFoundException e) {
            System.out.println("File does not exist");
            return null;
        } catch (IOException e) {
            System.out.println("IO Failed");
            return null;
        }
    }
}
