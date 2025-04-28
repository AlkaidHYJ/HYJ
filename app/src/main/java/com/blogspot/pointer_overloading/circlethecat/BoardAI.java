package com.blogspot.pointer_overloading.circlethecat;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Created by alhaad on 8/8/15.
 */
public class BoardAI {
    private final boolean[][] mBoard;
    private final int mXCatPos;
    private final int mYCatPos;

    BoardAI(boolean[][] board, int xCatPos, int yCatPos) {
        mBoard = board;
        mXCatPos = xCatPos;
        mYCatPos = yCatPos;
    }
    // Either a move 0-5 or -1 for Win or -2 for Loss.
    public int nextMove() {
        if (checkForLoss()) {
            return -2;
        }

        int move = -1;
        int shortestPath = Integer.MAX_VALUE;
        int probabitlitySelector = 1;
        Random r = new Random();
        // TODO(alhaad): Break into a functions.
        for (int i = 0; i < Board.BOARD_EDGE_SIZE; i++) {
            int j = 0;
            ArrayList<Integer> path = aStarPath(i, j);
            if (path.size() == 0) {
                continue;
            }
            if (path.size() < shortestPath) {
                shortestPath = path.size();
                move = path.get(path.size() - 1);
                probabitlitySelector = 1;
            } else if (path.size() == shortestPath && r.nextInt(100) < 100 / probabitlitySelector) {
                shortestPath = path.size();
                move = path.get(path.size() - 1);
                probabitlitySelector += 1;
            }

            j = Board.BOARD_EDGE_SIZE - 1;
            path = aStarPath(i, j);
            if (path.size() == 0) {
                continue;
            }
            if (path.size() < shortestPath) {
                shortestPath = path.size();
                move = path.get(path.size() - 1);
                probabitlitySelector = 1;
            } else if (path.size() == shortestPath && r.nextInt(100) < 100 / probabitlitySelector) {
                shortestPath = path.size();
                move = path.get(path.size() - 1);
                probabitlitySelector += 1;
            }
        }
        for (int j = 0; j < Board.BOARD_EDGE_SIZE; j++) {
            int i = 0;
            ArrayList<Integer> path = aStarPath(i, j);
            if (path.size() == 0) {
                continue;
            }
            if (path.size() < shortestPath) {
                shortestPath = path.size();
                move = path.get(path.size() - 1);
                probabitlitySelector = 1;
            } else if (path.size() == shortestPath && r.nextInt(100) < 100 / probabitlitySelector) {
                shortestPath = path.size();
                move = path.get(path.size() - 1);
                probabitlitySelector += 1;
            }

            i = Board.BOARD_EDGE_SIZE - 1;
            path = aStarPath(i, j);
            if (path.size() == 0) {
                continue;
            }
            if (path.size() < shortestPath) {
                shortestPath = path.size();
                move = path.get(path.size() - 1);
                probabitlitySelector = 1;
            } else if (path.size() == shortestPath && r.nextInt(100) < 100 / probabitlitySelector) {
                shortestPath = path.size();
                move = path.get(path.size() - 1);
                probabitlitySelector += 1;
            }
        }


        return move;
    }

    private class NodeComparator implements Comparator<Node> {

        @Override
        public int compare(Node lhs, Node rhs) {
            if (lhs.heuristicCost() < rhs.heuristicCost()) {
                return -1;
            } else if (lhs.heuristicCost() > rhs.heuristicCost()) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private class Node {
        private int mX;
        private int mY;
        private int realFromSrc;
        private int estimatedToGoal;
        private Node mParent;

        Node (int x, int y, int real, int xDest, int yDest, Node parent) {
            mX = x;
            mY = y;
            realFromSrc = real;
            estimatedToGoal = (int) Math.sqrt(Math.pow(x - xDest, 2) + Math.pow(y - yDest, 2));
            mParent = parent;
        }

        public int heuristicCost() {
            return  realFromSrc + estimatedToGoal;
        }

        public boolean isDestination(int xDest, int yDest) {
            if (mX == xDest && mY == yDest) {
                return true;
            }
            return false;
        }

        private void maybeAdd(int xPos, int yPos, int xDest, int yDest, ArrayList<Node> neighbours) {
            if (xPos < 0 || xPos >= Board.BOARD_EDGE_SIZE) {
                return;
            }
            if (yPos < 0 || yPos >= Board.BOARD_EDGE_SIZE) {
                return;
            }
            if (mBoard[xPos][yPos]) {
                return;
            }
            neighbours.add(new Node(xPos, yPos, realFromSrc + 1, xDest, yDest, this));
        }

        public ArrayList<Node> getValidNeighbours(int xDest, int yDest) {
            ArrayList<Node> neighbours = new ArrayList<>();
            int xPos, yPos;

            xPos = mX;
            if (mY % 2 == 0) {
                xPos -= 1;
            }
            yPos = mY + 1;
            maybeAdd(xPos, yPos, xDest, yDest, neighbours);

            xPos = mX - 1;
            yPos = mY;
            maybeAdd(xPos, yPos, xDest, yDest, neighbours);

            xPos = mX;
            if (mY % 2 == 0) {
                xPos -= 1;
            }
            yPos = mY - 1;
            maybeAdd(xPos, yPos, xDest, yDest, neighbours);

            xPos = mX;
            if (mY % 2 == 1) {
                xPos += 1;
            }
            yPos = mY - 1;
            maybeAdd(xPos, yPos, xDest, yDest, neighbours);

            xPos = mX + 1;
            yPos = mY;
            maybeAdd(xPos, yPos, xDest, yDest, neighbours);

            xPos = mX;
            if (mY % 2 == 1) {
                xPos += 1;
            }
            yPos = mY + 1;
            maybeAdd(xPos, yPos, xDest, yDest, neighbours);

            return neighbours;
        }

        public boolean isSameAs(Node n) {
            if (mX == n.mX && mY == n.mY) {
                return true;
            }
            return false;
        }

        public boolean maybeUpdateNode(Node current) {
            if (current.realFromSrc + 1 < realFromSrc) {
                realFromSrc = current.realFromSrc + 1;
                mParent = current;
                return true;
            }
            return false;
        }

        public void backfillMoves(ArrayList<Integer> moves) {
            Node current = this;
            while (current.mParent != null) {
                Node parent = current.mParent;
                int direction = -1;
                if (current.mY == parent.mY) {
                    if (current.mX == parent.mX + 1) {
                        direction = 4;
                    } else if (current.mX == parent.mX - 1) {
                        direction = 1;
                    }
                }
                if (current.mY == parent.mY + 1) {
                    if (parent.mY % 2 == 0) {
                        if (current.mX == parent.mX - 1) {
                            direction = 0;
                        } else if (current.mX == parent.mX) {
                            direction = 5;
                        }
                    } else {
                        if (current.mX == parent.mX) {
                            direction = 0;
                        } else if (current.mX == parent.mX + 1) {
                            direction = 5;
                        }
                    }
                }

                if (current.mY == parent.mY - 1) {
                    if (parent.mY % 2 == 0) {
                        if (current.mX == parent.mX - 1) {
                            direction = 2;
                        } else if (current.mX == parent.mX) {
                            direction = 3;
                        }
                    } else {
                        if (current.mX == parent.mX) {
                            direction = 2;
                        } else if (current.mX == parent.mX + 1) {
                            direction = 3;
                        }
                    }
                }
                if (direction == -1) {
                    System.out.println("Panic!!!!!!!!"  + current.mX + " " + current.mY + " " + parent.mX + " " + parent.mY);
                }
                moves.add(direction);

                current = parent;
            }
        }
    }

    ArrayList<Integer> aStarPath(int xDest, int yDest) {
        ArrayList<Integer> moves = new ArrayList<>();

        Comparator<Node> comparator = new NodeComparator();
        PriorityQueue<Node> openPositions = new PriorityQueue<>(100, comparator);
        openPositions.add(new Node(mXCatPos, mYCatPos, 0, xDest, yDest, null));

        ArrayList<Node> closedPositions = new ArrayList<>();

        while (!openPositions.isEmpty()) {
            Node current = openPositions.poll();
            if (current.isDestination(xDest, yDest)) {
                current.backfillMoves(moves);
                return moves;
            }
            closedPositions.add(current);

            ArrayList<Node> neighbours = current.getValidNeighbours(xDest, yDest);
            for (int i = 0; i < neighbours.size(); i++) {
                Node neighbour = neighbours.get(i);

                // Check for closed position.
                boolean isClosed = false;
                for (int j = 0; j < closedPositions.size(); j++) {
                    if (neighbour.isSameAs(closedPositions.get(j))) {
                        isClosed = true;
                        break;
                    }
                }
                if (isClosed) {
                    continue;
                }

                // Check for open position.
                boolean isOpen = false;
                Node updatedNode = null;
                Iterator<Node> it = openPositions.iterator();
                while (it.hasNext()) {
                    Node next = it.next();
                    if (neighbour.isSameAs(next)) {
                        isOpen = true;
                        if (next.maybeUpdateNode(current)) {
                            updatedNode = next;
                        }
                        break;
                    }
                }
                if (updatedNode != null) {
                    openPositions.remove(updatedNode);
                    openPositions.add(updatedNode);
                }
                if (!isOpen) {
                    openPositions.add(neighbour);
                }
            }

        }

        return moves;
    }


    private boolean checkForLoss() {
        if (mXCatPos <= 0 || mXCatPos >= Board.BOARD_EDGE_SIZE - 1 || mYCatPos <=0 || mYCatPos >= Board.BOARD_EDGE_SIZE - 1) {
            return true;
        }
        return false;
    }
}
