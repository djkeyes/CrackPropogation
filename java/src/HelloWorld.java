// testing some jni--fortran stuff
// this test mostly follows http://www.csharp.com/javacfort.html
// compile fortran into c, then use c as java native code. not too hard, just make sure to compile everything in the right order.
public class HelloWorld {
	
	public native int max(int[] arr);
	
	
	public static void main(String[] args){
		int[] foo = {1,2,3,4000, 5, 6};
		int m = new HelloWorld().max(foo);
		// this doesn't actually print the correct answer, because i don't know how to write syntactically-correct FORTRAN =/
		System.out.println("max is " + m);
	}
	
	static {
		// calls libmytest.so
        System.loadLibrary("mytest");
	}
}
