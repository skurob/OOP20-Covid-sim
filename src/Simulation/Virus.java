package Simulation;

import java.util.List;
import java.util.Random;
import java.util.HashSet;
import java.util.ArrayList;

/**
 *
 * @author rob
 */
public class Virus extends Thread{

    private float strenght;
    private Random rand;
    private volatile List<Person> crowd;
    private List<Person> infectedPeople;
    private int numPeople;
    private boolean isSpreading = false;
    private Infection infectionAlgo;

    public Virus(final List<Person> crowd, final float strenght) {
        this.strenght = strenght;
        this.crowd = crowd;
        this.numPeople = crowd.size();
        infectionAlgo = new Infection();

        rand = new Random();
    }

    public void startSpreading() {
        infectedPeople = new ArrayList<>();
        var unluckyBoi = crowd.get(rand.nextInt(numPeople));
        unluckyBoi.infect();

        infectedPeople.add(unluckyBoi);
        isSpreading = true;
    }

    public void stopSpreading() {
        isSpreading = false;
    }
    public void resumeSpreading(){
        isSpreading = true;
    }

    private void keepSpreading() {

        if (!isSpreading) {
            return;
        }

        for (var p : crowd) {
            if (p.isInfected()) {
                var nearPeople = p.getNearPeople();
                //System.out.println("Simulation.Virus.keepSpreading()");
                var allNearPeople = new HashSet<>(nearPeople);
                if (p.getLastNear() != null) {
                    nearPeople.removeAll(p.getLastNear());
                }

                nearPeople.forEach(person -> tryInfection(p, person));

                p.setLastNear(allNearPeople);
            }
        }

    }

    public boolean tryInfection(Person infector, Person victim) {

        if (infectionAlgo.apply(infector, victim)) {
            victim.infect();
            infectedPeople.add(victim);
            return true;
        }

        return false;
    }
    
    public int getInfectedNumb(){
        return infectedPeople.isEmpty() ? 0 : infectedPeople.size();
    }
    
    public void forceInfection(Person victim){
        victim.infect();
        infectedPeople.add(victim);
    }
    public void resumeInfected(){
        this.infectedPeople.forEach(i -> i.heal());
        this.infectedPeople.clear();
        //resume to 1 infected
        this.forceInfection(crowd.get(0));
    }
    public void updateCrowd(List<Person> p){
        this.crowd = p;
    }
    public List<Person> getCrowd(){
        return crowd;
    }
    public void update(float tpf) {
        keepSpreading();
    }

    @Override
    public void run() {
        startSpreading();
        while (isSpreading){
            keepSpreading();
            try { Thread.sleep(10); } catch(Exception ex) { }
        }
    }

}
