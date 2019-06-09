package com.iroha.model.parameter_objects;

import com.iroha.model.university.Speciality;
import com.iroha.model.university.University;

public class SpecialityChangeParameters {
    private final University sourceUniversity;
    private final University destinationUniversity;
    private final Speciality currentSpeciality;
    private final Speciality newSpeciality;

    public SpecialityChangeParameters(University sourceUniversity, University destinationUniversity, Speciality currentSpeciality, Speciality newSpeciality) {
        this.sourceUniversity = sourceUniversity;
        this.destinationUniversity = destinationUniversity;
        this.currentSpeciality = currentSpeciality;
        this.newSpeciality = newSpeciality;
    }

    public University getSourceUniversity() {
        return sourceUniversity;
    }

    public University getDestinationUniversity() {
        return destinationUniversity;
    }

    public Speciality getCurrentSpeciality() {
        return currentSpeciality;
    }

    public Speciality getNewSpeciality() {
        return newSpeciality;
    }
}
