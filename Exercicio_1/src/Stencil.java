public class Stencil {

    private double[] array;
    private double[] aux_array;
    private double[] old_array;

    private int num_iterations;

    Stencil(double []array,int num_iterations){
        this.array = array;
        this.aux_array = array.clone();
        this.old_array = array.clone();
        this.num_iterations = num_iterations;
    }

    private void transform(int idx){
        this.aux_array[idx] = (this.old_array[idx-1]+this.old_array[idx+1])/2;
    }


    private void swap(){
        this.array=this.aux_array;
        this.aux_array = this.old_array;
        this.old_array = this.array;
    }

    public void iterate(){
        for(int i=0; i<num_iterations;i++){
            for (int j = 1; j <this.array.length-1 ; j++) {
                transform(j);
            }
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