import random
import ast
import matplotlib.pyplot as plt
from matplotlib.patches import FancyArrowPatch
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


def draw_arrow_between_points(start, end, ax, zorder=2):
    """
    Draw a straight arrow between two points with adjusted properties for visibility.
    """
    arrowstyle = "-|>"  # Arrow style
    lw = 1.5  # Line width
    mutation_scale = 20  # Size of the arrow head

    # Create an arrow patch and add it to the axes with a specific zorder
    arrow = FancyArrowPatch(posA=start, posB=end, arrowstyle=arrowstyle,
                            mutation_scale=mutation_scale, lw=lw, color="b",
                            connectionstyle="arc3,rad=0.0", zorder=zorder)
    ax.add_patch(arrow)


def plotCitiesAndPath(C, N, Order):
    print("Start of plotCitiesAndPath function call")
    fig, ax = plt.subplots(figsize=(10, 9))


    # Plot the cities
    for i, (x, y) in enumerate(C):
        if i == 0:
            ax.plot(x, y, 'go', markersize=10, zorder=3)  # Mark the first city with a green circle
        else:
            ax.plot(x, y, 'ro', markersize=8, zorder=3)  # Plot other cities as red circles

    # Draw arrows between cities based on the order
    for i in range(len(Order) - 1):
        start_point = np.array(C[Order[i]])
        end_point = np.array(C[Order[i + 1]])
        draw_arrow_between_points(start_point, end_point, ax, zorder=2)

    # Optionally, draw an arrow from the last to the first city to complete the loop
    draw_arrow_between_points(np.array(C[Order[-1]]), np.array(C[Order[0]]), ax, zorder=2)

    # Set plot limits to always show a 50x50 grid
    ax.set_xlim(-50, 50)
    ax.set_ylim(-50, 50)

    # Set x and y ticks to mark every 10 units
    ax.set_xticks(np.arange(-50, 51, 1))
    ax.set_xticklabels([])
    ax.set_yticks(np.arange(-50, 51, 1))
    ax.set_yticklabels([])

    # Enable grid
    # ax.grid(True, which='both', linestyle='--', linewidth=0.5, zorder=0)
    # Remove the outline/spines
    for spine in ax.spines.values():
        spine.set_visible(False)
    ax.tick_params(axis='both', which='both', length=0)
    print("End of plotCitiesAndPath function call")


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

        # Parameters for the genetic algorithm
        population_size = 300
        tournament_size = min(10, len(C))
        crossover_rate = 0.8
        mutation_rate = 0.1
        num_generations = 100

        # Initialize the best path
        best_path = [0, 0] if len(C) > 1 else [0]

        if len(C) == 2:
            # Direct path calculation for two cities
            best_path = [0, 1, 0]
            distanceBefore = calcPath(best_path, C)
        elif len(C) > 2:
            # Generate initial population
            initial_population = generate_population(C, population_size)
            # Get the worst distance from the initial population
            distanceBefore = max(initial_population, key=lambda x: x[1])[1]

            # Run genetic algorithm
            genetic_best_path, convergence_data = genetic_algorithm(C, population_size, tournament_size, crossover_rate, mutation_rate, num_generations)
            best_path = [0] + genetic_best_path[0][1:] + [0]

        # Check and remove duplicate city at the end if necessary
        if len(best_path) > 1 and best_path[-1] == best_path[-2]:
            best_path.pop()

        plotCitiesAndPath(C, N, best_path)
        plt.savefig(output_path, transparent=True)


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