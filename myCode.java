import javax.naming.CannotProceedException;
import java.nio.file.Files;
import java.util.*;
import java.io.*;

class Simulation extends Block {
    public String algorithm;
    public int failedAlloc = 0;
    public List<Block> free_list;
    public List<Block> used_list;

    // Implements first fit
    public void alloc_first(int size) {
        Block block = null;
        ByOffset.exampleSort(free_list);

        // First fit
        for (int i = 0; i < free_list.size(); i++) {
            if (free_list.get(i).size >= size) {
                block = free_list.get(i);
                break;

            }  else {
                failedAlloc++;
                try {
                    throw new CannotProceedException("There is no available block");
                } catch (CannotProceedException e) {
                    e.printStackTrace();
                }
                splitBlock(block, "newB", size);
            }
        }
    }

    // Implements best fit
    public void alloc_best(int size) throws CannotProceedException {
        Block block = null;
        BySize.exampleSortbySize(free_list);

        // Best fit
        for (int i = 0; i < free_list.size(); i++) {
            if (free_list.get(i).size == size) {
                block = free_list.get(i);
                break;

            } else if (free_list.get(i).size > size) block = free_list.get(i);

            else {
                failedAlloc++;
                throw new CannotProceedException("There is no available block");
            }
            splitBlock(block, "newB", size);
        }
    }

    // Implements worst fit
    public void alloc_worst(int size){
        Block block = null;
        BySize.exampleSortbySize(free_list);

        // Worst fit
        for (int i = free_list.size(); i > 0 ; i--) {
            if ( free_list.get(i).size > size ) {
                block = free_list.get(i);
                break;

            } else if ( free_list.get(i).size == size ) block = free_list.get(i);

            else {
                failedAlloc++;
                try {
                    throw new CannotProceedException("There is no available block");
                } catch (CannotProceedException e) {
                    e.printStackTrace();
                }
            }
        }
        splitBlock(block,"newB", size);
    }

    // Implements random fit
    public void alloc_random(int size) {
        Block block = null;
        Collections.shuffle(free_list);

        // Random fit
        for (int i = 0; i < free_list.size(); i++) {
            if (free_list.get(i).size > size) {
                block = free_list.get(i);
                break;

            } else {
                failedAlloc++;
                try {
                    throw new CannotProceedException("There is no available block");
                } catch (CannotProceedException e) {
                    e.printStackTrace();
                }
                splitBlock(block, "newB", size);
            }
        }
    }

    // Implements next fit
    // FIX THIS
    public void alloc_next(int size) {
        int j = 0;
        int m = free_list.size();
        for ( int i = 0; i < m; i++ ) {
            while ( j < m ) {
                if ( free_list.get(i).size >= size ) {
                    free_list.remove(free_list.get(i));
                    used_list.add(free_list.get(i));
                } else {
                failedAlloc++;
                try {
                    throw new CannotProceedException("There is no available block");
                } catch (CannotProceedException e) {
                    e.printStackTrace();
                }
            }
                j = j + 1 % m;
            }
        }
    }

    /** This method takes in a string name for that block
     * and frees corresponding block
     *
     * @returns: nothing
     */
     public void free(String name) {
        for (Block b : used_list) {
            if (b.name.matches(name)) {
                used_list.remove(b);
                free_list.add(b);
            } else {
                throw new IllegalArgumentException("There does not exist a block with this name");
            }
            compactFreeList(free_list);
        }
    }

   /** This method takes in a Block, the size of the block and a name for that block
    * and splits it into necessary parts
    *
    * @return: nothing
    */
    private void splitBlock(Block b, String newBlock, int size) {
        /* #1 on worksheet */
        Block used;

        // if requested size is larger than block size
        if (size > b.size)
            throw new OutOfMemoryError("Chill, too large");

        // if request size and block size are the same; perfect match
        else if (size == b.size) {
            free_list.remove(b);
            used_list.remove(b);

         // create a new block
        } else {
            b.size -= size; // block size = block size - requested size
            used = new Block();
            used.offset = b.offset;
            used.size = size;
            b.offset += used.size;
            used_list.add(used);
        }
    }

    /** This method takes in a list of free blocks and constructs new list that "compacts"
     * adjacent blocks
     *
     * @ returns: nothing
     */
    public void compactFreeList(List<Block> free_list) {
        /* #3 on worksheet */
        List<Block> keep_list = null;
        Block accum = new Block();
        accum.offset = 0;
        accum.size = 0;

        for (Block b : free_list) {
            if (this.is_adjacent(accum, b)) {
                accum.size += b.size;
            } else {
                if (accum.size > 0) {
                    keep_list.add(accum);
                }
                accum = b;
            }
        }
        if ( accum.size > 0) {
            keep_list.add(accum);
        }
        this.free_list = keep_list;
    }

    public void sim_start(String algorithm, int size) {
        // pool first 1000
        this.algorithm = algorithm;
        this.size = size;
    }

    // ALGORITHM is one of "first", "best", "worst", "next" or "random".
    public void sim_alloc(String algorithm, int size) throws CannotProceedException {
//        if ( algorithm.equals("first") ) {
//            this.alloc_first(size);
//        }  else if ( algorithm.equals("best") ){
//            this.alloc_best(size);
//        }
        switch(algorithm){
            case "a" :  algorithm = "first";
                this.alloc_first(size);
                break;
            case "b":  algorithm = "best";
                this.alloc_best(size);
                break;
            case "c":  algorithm = "worst";
                this.alloc_worst(size);
                break;
            case "d":  algorithm = "next";
                this.alloc_next(size);
                break;
            case "e":  algorithm = "random";
                this.alloc_random(size);
                break;
        }
    }

    // Reads in a file and prints contents and other information
    public void main(String[] args) throws IOException, CannotProceedException {
        String[] cols;
        String name;
        String algorithm;
        int size;
        /* #9 on worksheet (modified) */
        // String file = "text.txt";
        for (String line : Files.readAllLines(new File(args[0]).toPath())) {
            cols = line.split("\\s+");

            System.out.println(Arrays.toString( line.split("\\s+") ) );

            if (cols[0].matches("pool")) {
                algorithm = cols[1];
                size = Integer.parseInt(cols[2]);
                sim_start(algorithm, size);
            } else if (cols[0].matches("alloc")) {
                name = cols[1];
                size = Integer.parseInt(cols[2]);
                sim_alloc(name, size);
            } else if (cols[0].matches("free")) {
                name = cols[1];
                free(name);
            } else {
                throw new IOException("Incorrect input");
            }
        }

        //Print list of used and free sections in offset order.
        this.print();

        // Print percentage of used, free memory after executing a file.
        this.percent();

        // Print number of failed allocations after executing a file.
        this.failedAlloc();
    }

    /**
     * Prints elements in free list and used list in offset order
     *
     * @return: nothing
     */
    public void print(){
        // loop thru free and used list and print contents
        System.out.println("Free List");
        ByOffset.exampleSort(free_list);
        for ( Block b : free_list) System.out.println(b);

        System.out.println("Used List");
        ByOffset.exampleSort(used_list);
        for ( Block b : used_list) System.out.println(b);


    }

    /** Outputs the percentage of used & free memory after executing a file.
     *
     * @returns: nothing
     */
    public void percent() {
        int freePercent = 0;
        int usedPercent = 0;
        int totalSize = used_list.size() + free_list.size();

        freePercent = ( free_list.size() / totalSize ) * 100;
        usedPercent = ( used_list.size() / totalSize ) * 100;

        System.out.println("% of used memory: " + usedPercent);
        System.out.println("% of free memory: " + freePercent);
    }

    /** Outputs the number of failed allocations after executing a file.
     *
     * @returns: nothing
     */
    public int failedAlloc() {
        return this.failedAlloc;
    }
}

// I’d want a block class with at least these fields and methods.
class Block {
    String name;
    int offset;
    int size;

  //  public String toString(); // highly recommended

    /** This method takes in two blocks and determines if they are adjacent
     *
     * @return: boolean (true or false)
     */
    public boolean is_adjacent(Block b1, Block b2) {
        /* #2 on worksheet*/
        if (b1.offset < b2.offset) {
            return b1.offset + b1.size == b2.offset;
        }

        return is_adjacent(b1, b2) || is_adjacent(b2, b1);
    }


}

// Sort-by in Java: (needs a class)
class ByOffset implements Comparator<Block> {

    @Override
    public int compare(Block lhs, Block rhs) {
        return Integer.compare(lhs.offset, rhs.offset);
    }

    static void exampleSort(List<Block> blocks) {
        Collections.sort(blocks, new ByOffset());
    }
}

class BySize implements Comparator<Block> {

    @Override
    public int compare(Block lhs, Block rhs) {
        return Integer.compare(lhs.size, rhs.size);
    }

    static void exampleSortbySize(List<Block> blocks) {
        Collections.sort(blocks, new BySize());
    }
}
