package com.pm.stack;

import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;
import software.amazon.awscdk.BootstraplessSynthesizer;
import software.amazon.awscdk.StackProps;



public class Main {
        public static void main(final String[] args) {
            App app = new App(AppProps.builder()
                    .outdir("./cdk.out") // This tells CDK where to put the output files
                    .build());

            // These are special properties for LocalStack
            StackProps props = StackProps.builder()
                    .synthesizer(new BootstraplessSynthesizer())
                    .build();

            // This line creates your stack
            new LocalStack(app, "LocalStack", props);

            // This command is what generates the cdk.out folder
            app.synth();
            System.out.println("App synthesizing completed.");
        }

    }
