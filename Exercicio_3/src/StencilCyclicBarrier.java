import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class StencilCyclicBarrier {

    private double[] array;
    private int num_iterations;
    private double[] aux_array;
    private int num_tasks;
    private int interval_size;
    private int num_elements;// number of elements to transform
    private CyclicBarrier barrier;

    StencilCyclicBarrier(double []array, int num_iterations, int num_tasks){
        this.array = array;
        this.num_elements = array.length-2;

        this.num_iterations = num_iterations;
        this.num_tasks = num_tasks <= num_elements ? num_tasks : num_elements; //at least one element per thread, otherwise num_threads is equal to num_tasks
        interval_size = get_interval_size();
        Runnable restart = () -> barrier.reset();
        barrier = new CyclicBarrier(this.num_tasks,restart);
    }


    private void transform(int start, int end){
        for (int i = start; i < end ; i++) {
            this.array[i] = (aux_array[i-1]+aux_array[i+1])/2;
        }
        try {
            this.barrier.await();//awaits other tasks/threads to start new iteration
        }catch (InterruptedException e)
        {
            e.printStackTrace();
        } catch (BrokenBarrierException e)
        {
        }
        }

    private int get_interval_size(){
        if((this.num_elements) > this.num_tasks) {
            return  (this.num_elements) / this.num_tasks;
        }else{
            return 1;
        }
    }


    private int get_interval_end(int start,int task) {
        if (task == num_tasks - 1) {//if last task/thread gets what is left -> rest of the  num_elements/num_tasks division
            return array.length - 1;  //task iterate until last element to be transformed
        } else {
            return start + this.interval_size;
        }
    }

    public void iterate() throws InterruptedException {
        for(int i=0; i<num_iterations;i++){
            this.aux_array = this.array.clone();
            for (int j = 1,task = 0; task < num_tasks ; j+=interval_size,task++) {
                int start = j;//first index of task interval
                int task_index = task;//index in phaser array
                new Thread(()-> transform(start,get_interval_end(start,task_index))).start();
            }
        }
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