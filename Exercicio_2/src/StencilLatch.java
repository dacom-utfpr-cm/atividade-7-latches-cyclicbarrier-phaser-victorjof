import java.util.concurrent.CountDownLatch;

public class StencilLatch {

    private double[] array;
    private double[] aux_array;
    private double[] old_array;
    private int num_iterations;
    private int num_tasks;
    private int interval_size;
    private int num_elements;// number of elements to transform

    private CountDownLatch count_latch;

    StencilLatch(double []array, int num_iterations, int num_tasks){

        this.array = array;
        this.aux_array = array.clone();
        this.old_array = array.clone();
        this.num_elements = array.length-2;
        count_latch = null;
        this.num_iterations = num_iterations;
        this.num_tasks = num_tasks <= num_elements ? num_tasks : num_elements; //at least one element per thread, otherwise num_threads is equal to num_tasks.
        interval_size = get_interval_size();

    }


    private void swap(){
        this.array=this.aux_array;
        this.aux_array = this.old_array;
        this.old_array = this.array;
    }

    private void transform(int start, int end){
        for (int i = start; i < end ; i++) {
            this.aux_array[i] = (old_array[i-1]+old_array[i+1])/2;
        }
        this.count_latch.countDown();//signals that assigned interval is transformed.
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
            this.count_latch = new CountDownLatch(num_tasks);
            for (int j = 1,task = 0; task < num_tasks ; j+=interval_size,task++) {
                int start = j;//first index of task interval
                int task_index = task;//index in phaser array
                new Thread(()-> transform(start,get_interval_end(start,task_index))).start();
            }
            count_latch.await();//awaits every task/thread to start new iteration
            swap();//updates array's new values after iteration

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