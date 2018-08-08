package com.iswoqqe.lox.lib;

import java.util.Stack;

public class PersistentHashMap<K, V> {
    private final INode root;
    private static final Object NOT_FOUND = new Object();

    public PersistentHashMap() {
        this.root = ArrayNode.EMPTY;
    }

    private PersistentHashMap(INode root) {
        this.root = root;
    }

    @SuppressWarnings("unchecked")
    public V get(K key) {
        Object o = root.find(0, key.hashCode(), key);
        return NOT_FOUND.equals(o) ? null : (V) o;
    }

    @SuppressWarnings("unchecked")
    public V get(K key, V notFound) {
        Object o = root.find(0, key.hashCode(), key);
        return NOT_FOUND.equals(o) ? notFound : (V) o;
    }

    public boolean hasKey(K key) {
        return ! NOT_FOUND.equals(root.find(0, key.hashCode(), key));
    }

    public PersistentHashMap<K, V> with(K key, V val) {
        return new PersistentHashMap<>(root.assoc(0, key.hashCode(), key, val));
    }

    private interface INode {
        INode assoc(int shift, int hash, Object key, Object val);
        Object find(int shift, int hash, Object key);
    }

    // Unused
    private static class LeafNode implements INode {
        private final int hash;
        private final Object key;
        private final Object val;

        LeafNode(int hash, Object key, Object val) {
            this.hash = hash;
            this.key = key;
            this.val = val;
        }

        @Override
        public INode assoc(int shift, int hash, Object key, Object val) {
            if (this.key.equals(key)) {
                return new LeafNode(hash, key, val);
            }

            if (shift >= 32) {
                return new LinkedListCollisionNode()
                        .assoc(shift, hash, key, val)
                        .assoc(shift, this.hash, this.key, this.val);
            }

            return IndexedNode.EMPTY
                    .assoc(shift, hash, key, val)
                    .assoc(shift, this.hash, this.key, this.val);
        }

        @Override
        public Object find(int shift, int hash, Object key) {
            if (this.key.equals(key)) {
                return this.val;
            }
            return NOT_FOUND;
        }
    }

    private static class IndexedNode implements INode {
        static final INode EMPTY = new IndexedNode(0, new INode[0]);

        private final int mask;
        private final Object[] array;

        IndexedNode(int mask, Object[] array) {
            this.mask = mask;
            this.array = array;
        }

        @Override
        public INode assoc(int shift, int hash, Object key, Object val) {
            int bit = getBit(shift, hash);
            int idx = getIdx(bit);

            if ((mask & bit) != 0) {
                Object keyOrNull = array[2*idx];
                Object valOrNode = array[2*idx + 1];

                if (keyOrNull == null) {
                    // insert into child-node
                    return new IndexedNode(mask, cloneAndSet(array, 2*idx + 1, ((INode) valOrNode).assoc(shift + 5, hash, key, val)));
                } else {
                    // create child-node, IndexedNode or CollisionNode
                    if (key.equals(keyOrNull)) {
                        if (nullSafeEquals(val, valOrNode)) {
                            return this;
                        }
                        return new IndexedNode(mask, cloneAndSet(array, 2*idx + 1, val));
                    }

                    INode newNode;

                    if (shift >= 30 && keyOrNull.hashCode() == hash) {
                        //newNode = ArrayCollisionNode.EMPTY
                        newNode = new LinkedListCollisionNode()
                                .assoc(shift + 5, hash, key, val)
                                .assoc(shift + 5, keyOrNull.hashCode(), keyOrNull, valOrNode);
                    } else {
                        newNode = EMPTY
                                .assoc(shift + 5, hash, key, val)
                                .assoc(shift + 5, keyOrNull.hashCode(), keyOrNull, valOrNode);
                    }

                    return new IndexedNode(mask, cloneAndSet(array, 2*idx, null, 2*idx + 1, newNode));
                }
            } else {
                int count = Integer.bitCount(mask);
                if (count >= 16) {
                    // create ArrayNode
                    INode[] newArray = new INode[32];

                    int newIdx = arrayNodeIdx(shift, hash);
                    newArray[newIdx] = EMPTY.assoc(shift + 5, hash, key, val);

                    int j = 0;
                    for (int i = 0; i < 32; ++i) {
                        if ((mask & (1 << i)) != 0) {
                            if (array[j] == null) {
                                newArray[i] = (INode) array[j + 1];
                            } else {
                                newArray[i] = IndexedNode.EMPTY.assoc(shift + 5, array[j].hashCode(), array[j], array[j + 1]);
                            }
                            j += 2;
                        }
                    }
                    return new ArrayNode(newArray);
                } else {
                    // add to array in copy of node
                    Object[] newArray = new Object[2*(count + 1)];
                    System.arraycopy(array, 0, newArray, 0, 2*idx);
                    newArray[2*idx] = key;
                    newArray[2*idx + 1] = val;
                    System.arraycopy(array, 2*idx, newArray, 2*(idx + 1), 2*(count - idx));
                    return new IndexedNode(mask | bit, newArray);
                }
            }
        }

        @Override
        public Object find(int shift, int hash, Object key) {
            int bit = getBit(shift, hash);
            if ((bit & mask) != 0) {
                int idx = getIdx(bit);

                Object keyOrNull = array[2*idx];
                Object valOrNode = array[2*idx + 1];

                if (keyOrNull == null) {
                    return ((INode) valOrNode).find(shift + 5, hash, key);
                } else {
                    return valOrNode;
                }
            }
            return NOT_FOUND;
        }

        private int getBit(int shift, int hash) {
            int maskedHash = (hash >>> shift) & 0x1f;
            return 1 << maskedHash;
        }

        private int getIdx(int bit) {
            return Integer.bitCount(mask & (bit - 1));
        }
    }

    private static class ArrayNode implements INode {
        static final ArrayNode EMPTY = new ArrayNode(new INode[32]);

        private final INode[] array;

        ArrayNode(INode[] array) {
            this.array = array;
        }

        @Override
        public INode assoc(int shift, int hash, Object key, Object val) {
            int idx = getIdx(shift, hash);
            INode node = array[idx];

            if (node == null) {
                return new ArrayNode(cloneAndSet(array, idx, IndexedNode.EMPTY.assoc(shift + 5, hash, key, val)));
            } else {
                return new ArrayNode(cloneAndSet(array, idx, node.assoc(shift + 5, hash, key, val)));
            }
        }

        @Override
        public Object find(int shift, int hash, Object key) {
            int idx = getIdx(shift, hash);
            INode node = array[idx];

            if (node == null) {
                return NOT_FOUND;
            }

            return node.find(shift + 5, hash, key);
        }

        private int getIdx(int shift, int hash) {
            return (hash >>> shift) & 0x1f;
        }
    }

    // Unused
    private static class ArrayCollisionNode implements INode {
        static final ArrayCollisionNode EMPTY = new ArrayCollisionNode(new Object[0]);

        private final Object[] array;

        ArrayCollisionNode(Object[] array) {
            this.array = array;
        }

        @Override
        public INode assoc(int shift, int hash, Object key, Object val) {
            int idx = findIndex(key);

           if (idx != -1) {
               if (array[idx + 1].equals(val)) {
                   return this;
               }
               return new ArrayCollisionNode(cloneAndSet(array, idx + 1, val));
           }

           Object[] newArray = new Object[array.length + 2];
           System.arraycopy(array, 0, newArray, 0, array.length);
           newArray[array.length] = key;
           newArray[array.length + 1] = val;
           return new ArrayCollisionNode(newArray);
        }

        @Override
        public Object find(int shift, int hash, Object key) {
            int idx = findIndex(key);

            if (idx != -1) {
                return array[idx + 1];
            }
            return NOT_FOUND;
        }

        private int findIndex(Object key) {
            for (int idx = 0; idx < array.length; idx += 2) {
                if (array[idx].equals(key)) {
                    return idx;
                }
            }
            return -1;
        }
    }

    private static class LinkedListCollisionNode implements INode {
        private class Node {
            private final Node next;
            private final Object key;
            private final Object val;

            Node(Node next, Object key, Object val) {
                this.next = next;
                this.key = key;
                this.val = val;
            }
        }

        private final Node head;

        LinkedListCollisionNode() {
            this.head = null;
        }

        private LinkedListCollisionNode(Node next, Object key, Object val) {
            this.head = new Node(next, key, val);
        }

        @Override
        public INode assoc(int shift, int hash, Object key, Object val) {
            boolean keyFound = false;
            Node node = head;
            Stack<Node> nodes = new Stack<>();

            while (node != null) {
                if (node.key.equals(key)) {
                    keyFound = true;
                    break;
                }

                nodes.push(node);
                node = node.next;
            }

            if (keyFound) {
                Node newNode = null;
                Node next = node.next;

                while (!nodes.empty()) {
                    Node curr = nodes.pop();
                    newNode = new Node(next, curr.key, curr.val);
                    next = curr.next;
                }

                return new LinkedListCollisionNode(newNode, key, val);
            }

            return new LinkedListCollisionNode(this.head, key, val);
        }

        @Override
        public Object find(int shift, int hash, Object key) {
            Node node = head;
            while (node != null) {
                if (node.key.equals(key)) {
                    return node.val;
                }
                node = node.next;
            }
            return NOT_FOUND;
        }
    }

    private static int arrayNodeIdx(int shift, int hash) {
        return (hash >>> shift) & 0x1f;
    }

    private static boolean nullSafeEquals(Object a, Object b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }

    private static Object[] cloneAndSet(Object[] array, int idx1, Object val1, int idx2, Object val2) {
        Object[] newArray = array.clone();
        newArray[idx1] = val1;
        newArray[idx2] = val2;
        return newArray;
    }

    private static Object[] cloneAndSet(Object[] array, int idx, Object val) {
        Object[] newArray = array.clone();
        newArray[idx] = val;
        return newArray;
    }

    private static INode[] cloneAndSet(INode[] array, int idx, INode val) {
        INode[] newArray = array.clone();
        newArray[idx] = val;
        return newArray;
    }
}
