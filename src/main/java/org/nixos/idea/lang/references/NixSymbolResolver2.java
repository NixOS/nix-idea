package org.nixos.idea.lang.references;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.lang.references.symbol.NixSymbol;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

final class NixSymbolResolver2 {

    private final @NotNull List<Result> myResults = new ArrayList<>();
    private final @NotNull Deque<Intermediate> myIntermediates = new ArrayDeque<>();

    @NotNull Collection<Result> resolve(int maxIterations) {
        List<Result> results = new ArrayList<>();
        resolve(results::add, maxIterations);
        return results;
    }

    private synchronized void resolve(Consumer<Result> consumer, int maxIterations) {
        int skips = 0;
        while (skips < myIntermediates.size()) {
            Intermediate next = myIntermediates.getFirst();
            if (next.requiredIterations > maxIterations) {
                myIntermediates.addLast(myIntermediates.removeFirst());
                skips += 1;
                continue;
            }
            skips = 0;
            doStep(next);
            myIntermediates.removeFirst();
        }
        myResults.sort(Comparator.comparing(Result::requiredIterations));
        myResults.stream()
                .takeWhile(r -> r.requiredIterations <= maxIterations)
                .forEach(consumer);
    }

    private void doStep(@NotNull Intermediate intermediate) {
        //
    }

    record Result(@NotNull NixSymbol symbol, @NotNull List<String> remainingAttributes, int requiredIterations) {}

    private record Intermediate(
            @Nullable Result backtrack,
            @NotNull Step nextStep,
            @NotNull List<String> attributes,
            int requiredIterations
    ) {}

    private interface Step {

        @Nullable NixSymbol symbol();

        @NotNull Collection<Result> resolve(@NotNull List<String> attributes, int maxIterations);
    }
}
