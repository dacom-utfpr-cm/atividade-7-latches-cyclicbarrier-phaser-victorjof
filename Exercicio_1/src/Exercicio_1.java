public class Exercicio_1 {
    public static void main(String[] args) throws InterruptedException {
        int num_elements = 100;
        double [] a = new double[num_elements+2];//at least size 2(edge elements)

        fill_array(a,42);

        Stencil stencil = new Stencil(a,num_elements*1000);//edge  value propagation requires more iterations for large arrays

        //transform all elements with array's edge value
        stencil.iterate();
        System.out.printf("array transformed:%n%s%n",stencil.toString());
    }

    private static void fill_array(double []a,int edge_value){
        for (int i = 0; i <a.length ; i++) {
            a[i] = i;
        }

        //defines edge
        a[0]=42;
        a[a.length-1]=a[0];
    }

}
