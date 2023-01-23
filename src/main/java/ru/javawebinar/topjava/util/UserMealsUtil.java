package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.mapping;
import static ru.javawebinar.topjava.util.TimeUtil.isBetweenHalfOpen;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);

        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        List<UserMealWithExcess> resultList = new ArrayList<>();
        HashMap<LocalDate, Integer> caloriesPerDayMap = new HashMap<>();

        for (UserMeal meal : meals) {
            LocalDate ld = meal.getDateTime().toLocalDate();
            int c = meal.getCalories();
            caloriesPerDayMap.merge(ld, c, Integer::sum);
        }

        for (UserMeal meal : meals) {
            LocalDateTime dt = meal.getDateTime();
            LocalDate ld = dt.toLocalDate();
            Integer calories = caloriesPerDayMap.get(ld);
            boolean excess = calories > caloriesPerDay;
            UserMealWithExcess userMealWithExcess = getUserMealWithExcess(meal, excess);

            LocalTime lt = dt.toLocalTime();
            if (isBetweenHalfOpen(lt, startTime, endTime)) {
                resultList.add(userMealWithExcess);
            }
        }

        return resultList;
    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> caloriesPerDayMap = meals.stream()
                .collect(Collectors
                        .groupingBy(userMeal -> userMeal.getDateTime().toLocalDate(),
                                mapping(UserMeal::getCalories, Collectors.summingInt(Integer::intValue))));

        return meals.stream()
                .filter(meal -> isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime))
                .map(meal -> getUserMealWithExcess(meal, caloriesPerDayMap.get(meal.getDateTime().toLocalDate()) > caloriesPerDay))
                .collect(Collectors.toList());
    }

    private static UserMealWithExcess getUserMealWithExcess(UserMeal meal, boolean excess) {
        return new UserMealWithExcess(
                meal.getDateTime(),
                meal.getDescription(),
                meal.getCalories(),
                excess
        );
    }
}
