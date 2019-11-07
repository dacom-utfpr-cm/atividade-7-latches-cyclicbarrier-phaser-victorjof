
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;

public class StencilPhaser {

    private double[] array;
    private int num_iterations;
    private double[] old_array;
    private double[] aux_array;
    private int num_tasks;
    private int interval_size;
    private int num_elements;// number of elements to transform
    private Phaser[] phasers;
    CountDownLatch tasks_completed;
    StencilPhaser(double []array, int num_iterations, int num_tasks){
        this.array = array;
        this.aux_array = array.clone();
        this.old_array = array.clone();
        this.num_elements = array.length-2;
        this.num_iterations = num_iterations;
        this.num_tasks = num_tasks <= num_elements ? num_tasks : num_elements; //at least one element per thread, otherwise num_threads is equal to num_tasks.
        interval_size = get_interval_size();
        phasers = new Phaser[num_tasks]; // num_tasks equals the number of threads, each task transforms n elements, where n = (num_elements / num_tasks)
        tasks_completed = new CountDownLatch(this.num_tasks);
    }



    private void transform(int idx){
        this.aux_array[idx] = (this.old_array[idx-1]+this.old_array[idx+1])/2;
    }


    private void transformations(int start, int end,int task){
        /*each task(thread) transforms a given interval of the array*/

        for(int j=0; j<num_iterations;j++) {
            //task's neighbours(task-1,task+1) only need to know the leftmost and rightmost values of the actual interval(start, end-1).
            transform(start);
            transform(end - 1);//no need to handle interval with size 1 -> operation is idempotent

            //Signal arrival for others tasks
            phasers[task].arrive();

            //transform interval's inside
            for (int i = start + 1; i < end - 1; i++) {
                transform(i);
            }
            swap();//updates array's new values after iteration
            wait_neighbors(task,j);
            //can proceed to next iteration
        }
        tasks_completed.countDown();
    }


    private void swap(){
        this.array=this.aux_array;
        this.aux_array = this.old_array;
        this.old_array = this.array;
    }

    private void wait_neighbors(int task,int phase){
        /*awaits for task's neighbors arrival at new phase*/

        if (task > 0) { //if it has left neighbor
            phasers[task - 1].awaitAdvance(phase);
        }
        if (task < num_tasks-1) {//if it has right neighbor
            phasers[task + 1].awaitAdvance(phase);
        }
    }


    private int get_interval_size(){
        if((this.num_elements) > this.num_tasks) {
            return  (this.num_elements) / this.num_tasks;
        }else{
            return 1;
        }
    }


    private int get_interval_end(int start,int task){
        if(task == num_tasks-1){//if last task/thread gets what is left -> rest of the  num_elements/num_tasks division
            return  array.length-1;  //task iterate until last element to be transformed
        }

        else{
            return start+this.interval_size;
        }

    }

    public void iterate() throws InterruptedException {
        for (int i = 0; i < phasers.length; i++) {this.phasers[i] = new Phaser(1);}
        for (int i = 1,task = 0; task < num_tasks ; i+=interval_size,task++) {
            int start = i;
            int task_index = task;//index in phaser array
            new Thread(()-> transformations(start,get_interval_end(start,task_index),task_index)).start();
        }
        tasks_completed.await();
    }



    public String toString(){
        String aux = "";
        aux += "[ ";

        for (Double value : this.array) {
            aux += (String.valueOf(Math.round(value+0.001)));//rounding bc the combination of: numeric method + IEE754, may take a long time to converge to the precise value.
            aux += " ";
        }
        aux += "]";
        return aux;
    }

}