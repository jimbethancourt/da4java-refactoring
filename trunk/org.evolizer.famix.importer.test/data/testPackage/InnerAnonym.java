package testPackage;

import java.nio.MappedByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import testPackage.IBase;

public class InnerAnonym  {
	private int nix = 0;
	
	public class HelperClass {
		public int compute(int x, int y) {
			return x + y;
		}
	}
	
	private static class MemoryMapPool {
		private static long MAX_SIZE = 100*1024*1024; //100MB
		private static long BLOCK_SIZE = 1024;
		private static final MemoryMapPool instance = new MemoryMapPool();
		private long total_size = 0;
		private final Map buffers = new LinkedHashMap( new Float(MAX_SIZE/BLOCK_SIZE).intValue(), .75F, true ){
			//This method is called just after a new entry has been added
			public boolean removeEldestEntry(Map.Entry eldest) {
				boolean remove = total_size > MAX_SIZE;
				if ( remove ) {
					MappedByteBuffer mbb = (MappedByteBuffer)eldest.getValue();
					total_size -= mbb.capacity();
				}
				return remove;
			}
		};
		
	  	private static void clean(final MappedByteBuffer buffer ) {
	  		InnerAnonym ia = new InnerAnonym();
	  		String x = "Die Summe ist " + (ia.new HelperClass() {
	  			public int compute(int x, int y) {
	  				return x + y - 1;
	  			} 			
	  		}.compute(10, 15));
	  		
//	  		AccessController.doPrivileged( new PrivilegedAction() {
//	  			public Object run() {
//	  				try {
//	  					FamixMethod getCleanerMethod = buffer.getClass().getMethod( "cleaner", new FamixClass[0] );
//	  					getCleanerMethod.setAccessible( true );
//	  					sun.misc.Cleaner cleaner = (sun.misc.Cleaner)getCleanerMethod.invoke( buffer, new Object[0] );
//	  					cleaner.clean();
//	  				}
//	          catch (Exception e) { Debug.printStackTrace( e ); }
//	  				return null;
//	  			}
//	  		});
	  	}
	  	
	  	
	  	private void foo(String a) {
		  	IBase myBase = new IBase() {
		  		public int compute() {
		  			
		  			IBase innerMYBase = new IBase() {
		  				public int compute() {
		  					return 21;
		  				}
		  			};
		  			return 42;
		  		}
		  	};

//		  	NotDef.call(); // activating this call will cause errors in the resolution of the anonymous types
	  	}
	}
}
