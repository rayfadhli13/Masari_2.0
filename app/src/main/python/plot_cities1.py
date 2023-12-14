import random
import ast
import matplotlib.pyplot as plt
from scipy.interpolate import make_interp_spline
import numpy as np
from scipy.interpolate import interp1d
from matplotlib.path import Path
import matplotlib.patches as patches
def read_data_from_file(file_path):
    try:
        with open(file_path, 'r') as file:
            data = file.read()
            C = ast.literal_eval(data.split('c = ')[1].split('\n')[0])
            N = ast.literal_eval(data.split('N = ')[1])
        return C, N
    except SyntaxError as e:
        print("Syntax error in data file:", e)
        return [], []


def bezier_curve(p1, p2, control_scale=0.5):
    t = np.linspace(0, 1, 100)
    midpoint_x = (p1[0] + p2[0]) / 2
    vertical_offset = control_scale * abs(p1[1] - p2[1]) / 2
    control_point = [midpoint_x, (p1[1] + p2[1]) / 2 - vertical_offset]
    curve = (1 - t)[:, None]**2 * p1 + 2 * (1 - t)[:, None] * t[:, None] * control_point + t[:, None]**2 * p2
    return curve


def plotCitiesAndPath(C, N, Order):
    print("Start of plotCitiesAndPath function call")
    maxVal = max([max(abs(x), abs(y)) for x, y in C])
    plt.figure(figsize=(10, 8))  # Smaller figure size

    for i in range(len(Order) - 1):
        p1 = np.array(C[Order[i]])
        p2 = np.array(C[Order[i + 1]])
        curve = bezier_curve(p1, p2)
        plt.plot(curve[:, 0], curve[:, 1], 'b', linestyle='-')

    plt.xlim(-maxVal - 1, maxVal + 1)
    plt.ylim(-maxVal - 1, maxVal + 1)
    plt.grid(True)
    plt.xticks(np.arange(-maxVal - 1, maxVal + 1, 1))
    plt.yticks(np.arange(-maxVal - 1, maxVal + 1, 1))

    for i, (x, y) in enumerate(C):
        marker_style = 'go' if i == 0 else 'ro'
        plt.plot(x, y, marker_style, markersize=10)
        plt.text(x, y, N[i], fontsize=14, ha='right', va='bottom')

    print("End plotCitiesAndPath function call")

def calcPath(Order, C):
    if len(Order) < 2:
        return 0
    total_distance = sum(np.sqrt((C[Order[i + 1]][0] - C[Order[i]][0]) ** 2 +
                                 (C[Order[i + 1]][1] - C[Order[i]][1]) ** 2)
                         for i in range(len(Order) - 1))
    return total_distance + np.sqrt((C[Order[-1]][0] - C[Order[0]][0]) ** 2 +
                                    (C[Order[-1]][1] - C[Order[0]][1]) ** 2)
def generate_plot_and_save(file_path, output_path):
    try:
        C, N = read_data_from_file(file_path)
        plt.figure(figsize=(10, 8))

        MaxITR = 100
        distanceBefore = float('inf')

        # Initialize best_path with a default value: start and return to the first city
        best_path = [0, 0]

        # Parameters for the genetic algorithm
        population_size = 100
        tournament_size = min(10, len(C))
        crossover_rate = 0.8
        mutation_rate = 0.1
        num_generations = 100

        if len(C) == 2:
            best_path = [0, 1, 0]
            distanceBefore = calcPath(best_path, C)
        else:
            for _ in range(MaxITR):
                Order = [0] + random.sample(range(1, len(C)), len(C) - 1) + [0]
                distance = calcPath(Order, C)

                if distance < distanceBefore:
                    distanceBefore = distance
                    best_path = Order

            if len(C) > 2:
                genetic_best_path, convergence_data = genetic_algorithm(C, population_size, tournament_size, crossover_rate, mutation_rate, num_generations)
                if genetic_best_path[1] < distanceBefore:
                    best_path = [0] + genetic_best_path[0][1:] + [0]

        # Check and remove duplicate city at the end if necessary
        if len(best_path) > 1 and best_path[-1] == best_path[-2]:
            best_path.pop()

        plotCitiesAndPath(C, N, best_path)
        plt.savefig(output_path)

        best_path_cities = [N[i] for i in best_path]
        distanceNow = calcPath(best_path, C)
        best_path_str = f"{best_path_cities}\n"
        distance_before_str = f"{distanceBefore:.2f}"
        best_distance_str = f"{distanceNow:.2f}"

        print(f"End of generate_plot_and_save function call file path [{file_path}], output path [{output_path}]")
    except Exception as e:
        print(e)
        raise e

    return best_path_str, distance_before_str, best_distance_str

def generate_population(cities, population_size):
    population = []
    num_cities = len(cities)
    for _ in range(population_size):
        intermediate_cities = list(range(1, num_cities))
        random.shuffle(intermediate_cities)
        path = [0] + intermediate_cities + [0]
        distance = calcPath(path, cities)
        population.append((path, distance))
    return population
def tournament_selection(population, tournament_size):
    tournament = random.sample(population, tournament_size)
    return min(tournament, key=lambda x: x[1])
def crossover(parent1, parent2):
    num_cities = len(parent1)
    if num_cities <= 3:
        return parent1
    else:
        start, end = sorted(random.sample(range(1, num_cities - 1), 2))
        child = [-1] * num_cities
        child[0] = 0
        child[start:end] = parent1[start:end]
        idx = end
        for city in parent2:
            if city not in child and city != 0:
                child[idx] = city
                idx = (idx + 1) % (num_cities - 1)
        child[-1] = 0
        return child
def mutation(path):
    num_cities = len(path)
    idx1, idx2 = random.sample(range(1, num_cities - 1), 2)
    path[idx1], path[idx2] = path[idx2], path[idx1]
    return path
def genetic_algorithm(cities, population_size, tournament_size, crossover_rate, mutation_rate, num_generations):
    population = generate_population(cities, population_size)
    best_path = min(population, key=lambda x: x[1])
    best_distance = best_path[1]
    convergence_data = []
    for generation in range(num_generations):
        new_population = []
        while len(new_population) < population_size:
            parent1 = tournament_selection(population, tournament_size)
            parent2 = tournament_selection(population, tournament_size)
            if random.random() < crossover_rate:
                child = crossover(parent1[0], parent2[0])
            else:
                child = parent1[0]
            if random.random() < mutation_rate:
                child = mutation(child)
            distance = calcPath(child, cities)
            new_population.append((child, distance))
        population = new_population
        best_path = min(population, key=lambda x: x[1])
        best_distance = best_path[1]
        convergence_data.append(best_distance)
        if generation % 10 == 0:
            print(f"Generation {generation}: Best Distance = {best_distance}")
    return best_path, convergence_data