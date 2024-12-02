package org.example;

import java.util.*;

class KnapsackGA {
    static final int CAPACITY = 250;
    static final int NUM_ITEMS = 100;
    static final int POPULATION_SIZE = 100;
    static final int MAX_GENERATIONS = 1000;
    static final int LOG_INTERVAL = 20;
    static final double CROSSOVER_RATE = 0.25;
    static final double MUTATION_RATE = 0.05;

    static class Item {
        int value, weight;

        Item(int value, int weight) {
            this.value = value;
            this.weight = weight;
        }
    }

    static class Individual {
        boolean[] genes;
        int fitness;

        Individual(boolean[] genes) {
            this.genes = genes;
            this.fitness = 0;
        }

        void calculateFitness(Item[] items) {
            int totalValue = 0, totalWeight = 0;
            for (int i = 0; i < genes.length; i++) {
                if (genes[i]) {
                    totalValue += items[i].value;
                    totalWeight += items[i].weight;
                }
            }
            fitness = (totalWeight <= CAPACITY) ? totalValue : 0;
        }
    }

    static Individual[] initializePopulation() {
        Individual[] population = new Individual[POPULATION_SIZE];
        for (int i = 0; i < POPULATION_SIZE; i++) {
            boolean[] genes = new boolean[NUM_ITEMS];
            genes[i % NUM_ITEMS] = true;
            population[i] = new Individual(genes);
        }
        return population;
    }

    static Individual[] select(Individual[] population) {
        Arrays.sort(population, Comparator.comparingInt(ind -> -ind.fitness));
        return Arrays.copyOfRange(population, 0, POPULATION_SIZE / 2);
    }

    static Individual crossover(Individual parent1, Individual parent2) {
        boolean[] childGenes = new boolean[NUM_ITEMS];
        Random rand = new Random();
        int point1 = rand.nextInt(NUM_ITEMS);
        int point2 = rand.nextInt(NUM_ITEMS);
        int point3 = rand.nextInt(NUM_ITEMS);
        int[] points = {point1, point2, point3};
        Arrays.sort(points);

        for (int i = 0; i < NUM_ITEMS; i++) {
            if (i <= points[0] || (i > points[1] && i <= points[2])) {
                childGenes[i] = parent1.genes[i];
            } else {
                childGenes[i] = parent2.genes[i];
            }
        }
        return new Individual(childGenes);
    }

    static void mutate(Individual individual) {
        Random rand = new Random();
        if (rand.nextDouble() < MUTATION_RATE) {
            int index1 = rand.nextInt(NUM_ITEMS);
            int index2 = rand.nextInt(NUM_ITEMS);
            boolean temp = individual.genes[index1];
            individual.genes[index1] = individual.genes[index2];
            individual.genes[index2] = temp;
        }
    }

    static void localImprovement(Individual individual, Item[] items) {
        int totalWeight = 0;
        for (int i = 0; i < NUM_ITEMS; i++) {
            if (individual.genes[i]) {
                totalWeight += items[i].weight;
            }
        }

        if (totalWeight > CAPACITY) {
            Random rand = new Random();
            for (int i = 0; i < NUM_ITEMS; i++) {
                if (individual.genes[i] && rand.nextBoolean()) {
                    individual.genes[i] = false;
                    totalWeight -= items[i].weight;
                    if (totalWeight <= CAPACITY) break;
                }
            }
        } else {
            for (int i = 0; i < NUM_ITEMS; i++) {
                if (!individual.genes[i] && totalWeight + items[i].weight <= CAPACITY) {
                    individual.genes[i] = true;
                    totalWeight += items[i].weight;
                }
            }
        }
    }

    public static void main(String[] args) {
        Random rand = new Random();
        Item[] items = new Item[NUM_ITEMS];
        for (int i = 0; i < NUM_ITEMS; i++) {
            items[i] = new Item(rand.nextInt(29) + 2, rand.nextInt(24) + 1);
        }

        Individual[] population = initializePopulation();
        for (Individual ind : population) {
            ind.calculateFitness(items);
        }

        List<Integer> fitnessLog = new ArrayList<>();

        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            Individual[] selected = select(population);

            List<Individual> nextGeneration = new ArrayList<>(Arrays.asList(selected));
            while (nextGeneration.size() < POPULATION_SIZE) {
                if (rand.nextDouble() < CROSSOVER_RATE) {
                    Individual parent1 = selected[rand.nextInt(selected.length)];
                    Individual parent2 = selected[rand.nextInt(selected.length)];
                    Individual child = crossover(parent1, parent2);
                    mutate(child);
                    child.calculateFitness(items);
                    localImprovement(child, items);
                    nextGeneration.add(child);
                }
            }

            population = nextGeneration.toArray(new Individual[0]);
            for (Individual ind : population) {
                ind.calculateFitness(items);
            }

            if (generation % LOG_INTERVAL == 0) {
                Individual best = Arrays.stream(population).max(Comparator.comparingInt(ind -> ind.fitness)).orElse(null);
                fitnessLog.add(best.fitness);
                System.out.println("Iteration " + generation + ": Best fitness = " + best.fitness);
            }
        }

        System.out.println("Fitness log: " + fitnessLog);
    }
}
