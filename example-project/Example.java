public class Example {
    private int value = 0;
    
    public void increment() {
        value++;
    }
    
    public void decrement() {
        value--;
    }
    
    public void reset() {
        value = 0;
    }
    
    public void performOperations() {
        // These method calls can be prefixed with 'this.' using the plugin
      increment();
      decrement();
      reset();
    }
    
    public void performOperationsWithThis() {
        // These already have 'this.' prefix and should be ignored by the plugin
        this.increment();
        this.decrement();
        this.reset();
    }
    
    public static void staticMethod() {
        System.out.println("Static method");
    }
    
    public void callStaticMethod() {
        // Static method calls should be ignored by the plugin
        staticMethod();
    }
}