import random
import ast
import matplotlib.pyplot as plt
from scipy.interpolate import make_interp_spline
import numpy as np
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
def plotCitiesAndPath(C, N, Order):
    print("Start of plotCitiesAndPath function call")
    maxVal = max([max(abs(x), abs(y)) for x, y in C])
    print("Plot Cities")
    plt.figure()
    print("Prepare data for the path")
    x_path = [C[i][0] for i in Order] + [C[Order[0]][0]]
    y_path = [C[i][1] for i in Order] + [C[Order[0]][1]]
    print("Interpolate to create a smooth path")
    if len(x_path) > 1 and len(y_path) > 1:
        path_points = 200
        t = np.linspace(0, 1, len(x_path))
        t_new = np.linspace(0, 1, path_points)
        spline_x = make_interp_spline(t, x_path, k=2)
        spline_y = make_interp_spline(t, y_path, k=2)
        x_smooth = spline_x(t_new)
        y_smooth = spline_y(t_new)
    else:
        x_smooth, y_smooth = x_path, y_path
    print("Plot the path")
    plt.plot(x_smooth, y_smooth, 'b', linestyle='-')
    print("Set plot limits and grid")
    plt.xlim(-maxVal - 1, maxVal + 1)
    plt.ylim(-maxVal - 1, maxVal + 1)
    plt.grid(True)
    plt.xticks(range(-maxVal - 1, maxVal + 1, 1))
    plt.yticks(range(-maxVal - 1, maxVal + 1, 1))
    for i, (x, y) in enumerate(C):
        if i == 0:
            plt.plot(x, y, 'go', markersize=10)  # Mark the first city with a green circle
        else:
            plt.plot(x, y, 'ro', markersize=8)  # Plot other cities as red circles
        plt.text(x, y, N[i], fontsize=14, ha='right', va='bottom')  # Label cities
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
        plt.figure(figsize=(10, 10))
        if len(C) <= 2:
            Order = list(range(len(C))) + [0]
            distanceBefore = calcPath(Order, C)
            plotCitiesAndPath(C, N, Order)
        else:
            MaxITR = 100
            distanceBefore = float('inf')
            population_size = 100
            tournament_size = min(10, len(C))
            crossover_rate = 0.8
            mutation_rate = 0.1
            num_generations = 100
            for _ in range(MaxITR):
                if len(C) > 1:
                    Order = [0] + random.sample(range(1, len(C)), len(C) - 1)
                else:
                    Order = list(range(len(C)))
                distance = calcPath(Order, C)
                if distance < distanceBefore:
                    distanceBefore = distance
            best_path, convergence_data = genetic_algorithm(C, population_size, tournament_size, crossover_rate, mutation_rate, num_generations)
            print(f"Best Path: {best_path[0]}")
            print(f"Best Distance: {best_path[1]}")
            Order = best_path[0] + [0]
            plotCitiesAndPath(C, N, Order)
        plt.savefig(output_path)
        print("Prepare the best path and total distance as a string")
        best_path_cities = [N[i] for i in Order]
        distanceNow = calcPath(Order, C)
        result_str = f"Distance Before: {distanceBefore:.2f}\nDistance Now: {distanceNow:.2f}\nBest Path: {best_path_cities}"
        print(result_str)
        print(f"End of generate_plot_and_save function call file path [{file_path}], output path [{output_path}]")
    except Exception as e:
        print(e)
        raise e
    return result_str
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