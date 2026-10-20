package sk.upjs.ics.application.runners;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WindowsApplicationRunner /*implements IApplicationRunner*/ {
    private final RunnerSpecifications _specifications;

    public final RunnerKind kind() { return RunnerKind.WINDOWS_NATIVE; };

    //@Override
    public void run() {
        // implement this
    }
}
