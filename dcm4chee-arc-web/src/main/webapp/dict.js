/**
 * Created by aprta on 5/8/2014.
 */

var dictionary = {
    "00020000" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "FileMetaInformationGroupLength"
    },
    "00020001" : {
        "VR" : "OB",
        "VM" : "1",
        "KEYWORD" : "FileMetaInformationVersion"
    },
    "00020002" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "MediaStorageSOPClassUID"
    },
    "00020003" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "MediaStorageSOPInstanceUID"
    },
    "00020010" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "TransferSyntaxUID"
    },
    "00020012" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "ImplementationClassUID"
    },
    "00020013" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "ImplementationVersionName"
    },
    "00020016" : {
        "VR" : "AE",
        "VM" : "1",
        "KEYWORD" : "SourceApplicationEntityTitle"
    },
    "00020100" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "PrivateInformationCreatorUID"
    },
    "00020102" : {
        "VR" : "OB",
        "VM" : "1",
        "KEYWORD" : "PrivateInformation"
    },
    "00041130" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FileSetID"
    },
    "00041141" : {
        "VR" : "CS",
        "VM" : "1-8",
        "KEYWORD" : "FileSetDescriptorFileID"
    },
    "00041142" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SpecificCharacterSetOfFileSetDescriptorFile"
    },
    "00041200" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "OffsetOfTheFirstDirectoryRecordOfTheRootDirectoryEntity"
    },
    "00041202" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "OffsetOfTheLastDirectoryRecordOfTheRootDirectoryEntity"
    },
    "00041212" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "FileSetConsistencyFlag"
    },
    "00041220" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DirectoryRecordSequence"
    },
    "00041400" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "OffsetOfTheNextDirectoryRecord"
    },
    "00041410" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "RecordInUseFlag"
    },
    "00041420" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "OffsetOfReferencedLowerLevelDirectoryEntity"
    },
    "00041430" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DirectoryRecordType"
    },
    "00041432" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "PrivateRecordUID"
    },
    "00041500" : {
        "VR" : "CS",
        "VM" : "1-8",
        "KEYWORD" : "ReferencedFileID"
    },
    "00041504" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "MRDRDirectoryRecordOffset"
    },
    "00041510" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "ReferencedSOPClassUIDInFile"
    },
    "00041511" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "ReferencedSOPInstanceUIDInFile"
    },
    "00041512" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "ReferencedTransferSyntaxUIDInFile"
    },
    "0004151A" : {
        "VR" : "UI",
        "VM" : "1-n",
        "KEYWORD" : "ReferencedRelatedGeneralSOPClassUIDInFile"
    },
    "00041600" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "NumberOfReferences"
    },
    "00080001" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "LengthToEnd"
    },
    "00080005" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "SpecificCharacterSet"
    },
    "00080006" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "LanguageCodeSequence"
    },
    "00080008" : {
        "VR" : "CS",
        "VM" : "2-n",
        "KEYWORD" : "ImageType"
    },
    "00080010" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "RecognitionCode"
    },
    "00080012" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "InstanceCreationDate"
    },
    "00080013" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "InstanceCreationTime"
    },
    "00080014" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "InstanceCreatorUID"
    },
    "00080016" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "SOPClassUID"
    },
    "00080018" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "SOPInstanceUID"
    },
    "0008001A" : {
        "VR" : "UI",
        "VM" : "1-n",
        "KEYWORD" : "RelatedGeneralSOPClassUID"
    },
    "0008001B" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "OriginalSpecializedSOPClassUID"
    },
    "00080020" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "StudyDate"
    },
    "00080021" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "SeriesDate"
    },
    "00080022" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "AcquisitionDate"
    },
    "00080023" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "ContentDate"
    },
    "00080024" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "OverlayDate"
    },
    "00080025" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "CurveDate"
    },
    "0008002A" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "AcquisitionDateTime"
    },
    "00080030" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "StudyTime"
    },
    "00080031" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "SeriesTime"
    },
    "00080032" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "AcquisitionTime"
    },
    "00080033" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "ContentTime"
    },
    "00080034" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "OverlayTime"
    },
    "00080035" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "CurveTime"
    },
    "00080040" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "DataSetType"
    },
    "00080041" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "DataSetSubtype"
    },
    "00080042" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "NuclearMedicineSeriesType"
    },
    "00080050" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "AccessionNumber"
    },
    "00080051" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IssuerOfAccessionNumberSequence"
    },
    "00080052" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "QueryRetrieveLevel"
    },
    "00080054" : {
        "VR" : "AE",
        "VM" : "1-n",
        "KEYWORD" : "RetrieveAETitle"
    },
    "00080056" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "InstanceAvailability"
    },
    "00080058" : {
        "VR" : "UI",
        "VM" : "1-n",
        "KEYWORD" : "FailedSOPInstanceUIDList"
    },
    "00080060" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "Modality"
    },
    "00080061" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "ModalitiesInStudy"
    },
    "00080062" : {
        "VR" : "UI",
        "VM" : "1-n",
        "KEYWORD" : "SOPClassesInStudy"
    },
    "00080064" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ConversionType"
    },
    "00080068" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PresentationIntentType"
    },
    "00080070" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "Manufacturer"
    },
    "00080080" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "InstitutionName"
    },
    "00080081" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "InstitutionAddress"
    },
    "00080082" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "InstitutionCodeSequence"
    },
    "00080090" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "ReferringPhysicianName"
    },
    "00080092" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "ReferringPhysicianAddress"
    },
    "00080094" : {
        "VR" : "SH",
        "VM" : "1-n",
        "KEYWORD" : "ReferringPhysicianTelephoneNumbers"
    },
    "00080096" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferringPhysicianIdentificationSequence"
    },
    "00080100" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "CodeValue"
    },
    "00080102" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "CodingSchemeDesignator"
    },
    "00080103" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "CodingSchemeVersion"
    },
    "00080104" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "CodeMeaning"
    },
    "00080105" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "MappingResource"
    },
    "00080106" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "ContextGroupVersion"
    },
    "00080107" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "ContextGroupLocalVersion"
    },
    "0008010B" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ContextGroupExtensionFlag"
    },
    "0008010C" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "CodingSchemeUID"
    },
    "0008010D" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "ContextGroupExtensionCreatorUID"
    },
    "0008010F" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ContextIdentifier"
    },
    "00080110" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CodingSchemeIdentificationSequence"
    },
    "00080112" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "CodingSchemeRegistry"
    },
    "00080114" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "CodingSchemeExternalID"
    },
    "00080115" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "CodingSchemeName"
    },
    "00080116" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "CodingSchemeResponsibleOrganization"
    },
    "00080117" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "ContextUID"
    },
    "00080201" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "TimezoneOffsetFromUTC"
    },
    "00081000" : {
        "VR" : "AE",
        "VM" : "1",
        "KEYWORD" : "NetworkID"
    },
    "00081010" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "StationName"
    },
    "00081030" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "StudyDescription"
    },
    "00081032" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ProcedureCodeSequence"
    },
    "0008103E" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SeriesDescription"
    },
    "0008103F" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SeriesDescriptionCodeSequence"
    },
    "00081040" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "InstitutionalDepartmentName"
    },
    "00081048" : {
        "VR" : "PN",
        "VM" : "1-n",
        "KEYWORD" : "PhysiciansOfRecord"
    },
    "00081049" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PhysiciansOfRecordIdentificationSequence"
    },
    "00081050" : {
        "VR" : "PN",
        "VM" : "1-n",
        "KEYWORD" : "PerformingPhysicianName"
    },
    "00081052" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PerformingPhysicianIdentificationSequence"
    },
    "00081060" : {
        "VR" : "PN",
        "VM" : "1-n",
        "KEYWORD" : "NameOfPhysiciansReadingStudy"
    },
    "00081062" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PhysiciansReadingStudyIdentificationSequence"
    },
    "00081070" : {
        "VR" : "PN",
        "VM" : "1-n",
        "KEYWORD" : "OperatorsName"
    },
    "00081072" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OperatorIdentificationSequence"
    },
    "00081080" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "AdmittingDiagnosesDescription"
    },
    "00081084" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AdmittingDiagnosesCodeSequence"
    },
    "00081090" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ManufacturerModelName"
    },
    "00081100" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedResultsSequence"
    },
    "00081110" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedStudySequence"
    },
    "00081111" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedPerformedProcedureStepSequence"
    },
    "00081115" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedSeriesSequence"
    },
    "00081120" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedPatientSequence"
    },
    "00081125" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedVisitSequence"
    },
    "00081130" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedOverlaySequence"
    },
    "00081134" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedStereometricInstanceSequence"
    },
    "0008113A" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedWaveformSequence"
    },
    "00081140" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedImageSequence"
    },
    "00081145" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedCurveSequence"
    },
    "0008114A" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedInstanceSequence"
    },
    "0008114B" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedRealWorldValueMappingInstanceSequence"
    },
    "00081150" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "ReferencedSOPClassUID"
    },
    "00081155" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "ReferencedSOPInstanceUID"
    },
    "0008115A" : {
        "VR" : "UI",
        "VM" : "1-n",
        "KEYWORD" : "SOPClassesSupported"
    },
    "00081160" : {
        "VR" : "IS",
        "VM" : "1-n",
        "KEYWORD" : "ReferencedFrameNumber"
    },
    "00081161" : {
        "VR" : "UL",
        "VM" : "1-n",
        "KEYWORD" : "SimpleFrameList"
    },
    "00081162" : {
        "VR" : "UL",
        "VM" : "3-3n",
        "KEYWORD" : "CalculatedFrameList"
    },
    "00081163" : {
        "VR" : "FD",
        "VM" : "2",
        "KEYWORD" : "TimeRange"
    },
    "00081164" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FrameExtractionSequence"
    },
    "00081167" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "MultiFrameSourceSOPInstanceUID"
    },
    "00081190" : {
        "VR" : "UT",
        "VM" : "1",
        "KEYWORD" : "RetrieveURL"
    },
    "00081195" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "TransactionUID"
    },
    "00081196" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "WarningReason"
    },
    "00081197" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "FailureReason"
    },
    "00081198" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FailedSOPSequence"
    },
    "00081199" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedSOPSequence"
    },
    "00081200" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "StudiesContainingOtherReferencedInstancesSequence"
    },
    "00081250" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RelatedSeriesSequence"
    },
    "00082110" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "LossyImageCompressionRetired"
    },
    "00082111" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "DerivationDescription"
    },
    "00082112" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SourceImageSequence"
    },
    "00082120" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "StageName"
    },
    "00082122" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "StageNumber"
    },
    "00082124" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfStages"
    },
    "00082127" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "ViewName"
    },
    "00082128" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ViewNumber"
    },
    "00082129" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfEventTimers"
    },
    "0008212A" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfViewsInStage"
    },
    "00082130" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "EventElapsedTimes"
    },
    "00082132" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "EventTimerNames"
    },
    "00082133" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "EventTimerSequence"
    },
    "00082134" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "EventTimeOffset"
    },
    "00082135" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "EventCodeSequence"
    },
    "00082142" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "StartTrim"
    },
    "00082143" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "StopTrim"
    },
    "00082144" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "RecommendedDisplayFrameRate"
    },
    "00082200" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TransducerPosition"
    },
    "00082204" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TransducerOrientation"
    },
    "00082208" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AnatomicStructure"
    },
    "00082218" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AnatomicRegionSequence"
    },
    "00082220" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AnatomicRegionModifierSequence"
    },
    "00082228" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PrimaryAnatomicStructureSequence"
    },
    "00082229" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AnatomicStructureSpaceOrRegionSequence"
    },
    "00082230" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PrimaryAnatomicStructureModifierSequence"
    },
    "00082240" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TransducerPositionSequence"
    },
    "00082242" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TransducerPositionModifierSequence"
    },
    "00082244" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TransducerOrientationSequence"
    },
    "00082246" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TransducerOrientationModifierSequence"
    },
    "00082251" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AnatomicStructureSpaceOrRegionCodeSequenceTrial"
    },
    "00082253" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AnatomicPortalOfEntranceCodeSequenceTrial"
    },
    "00082255" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AnatomicApproachDirectionCodeSequenceTrial"
    },
    "00082256" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "AnatomicPerspectiveDescriptionTrial"
    },
    "00082257" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AnatomicPerspectiveCodeSequenceTrial"
    },
    "00082258" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "AnatomicLocationOfExaminingInstrumentDescriptionTrial"
    },
    "00082259" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AnatomicLocationOfExaminingInstrumentCodeSequenceTrial"
    },
    "0008225A" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AnatomicStructureSpaceOrRegionModifierCodeSequenceTrial"
    },
    "0008225C" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OnAxisBackgroundAnatomicStructureCodeSequenceTrial"
    },
    "00083001" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AlternateRepresentationSequence"
    },
    "00083010" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "IrradiationEventUID"
    },
    "00084000" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "IdentifyingComments"
    },
    "00089007" : {
        "VR" : "CS",
        "VM" : "4",
        "KEYWORD" : "FrameType"
    },
    "00089092" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedImageEvidenceSequence"
    },
    "00089121" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedRawDataSequence"
    },
    "00089123" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "CreatorVersionUID"
    },
    "00089124" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DerivationImageSequence"
    },
    "00089154" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SourceImageEvidenceSequence"
    },
    "00089205" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PixelPresentation"
    },
    "00089206" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "VolumetricProperties"
    },
    "00089207" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "VolumeBasedCalculationTechnique"
    },
    "00089208" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ComplexImageComponent"
    },
    "00089209" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AcquisitionContrast"
    },
    "00089215" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DerivationCodeSequence"
    },
    "00089237" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedPresentationStateSequence"
    },
    "00089410" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedOtherPlaneSequence"
    },
    "00089458" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FrameDisplaySequence"
    },
    "00089459" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "RecommendedDisplayFrameRateInFloat"
    },
    "00089460" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SkipFrameRangeFlag"
    },
    "00100010" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "PatientName"
    },
    "00100020" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PatientID"
    },
    "00100021" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "IssuerOfPatientID"
    },
    "00100022" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TypeOfPatientID"
    },
    "00100024" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IssuerOfPatientIDQualifiersSequence"
    },
    "00100030" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "PatientBirthDate"
    },
    "00100032" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "PatientBirthTime"
    },
    "00100040" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PatientSex"
    },
    "00100050" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PatientInsurancePlanCodeSequence"
    },
    "00100101" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PatientPrimaryLanguageCodeSequence"
    },
    "00100102" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PatientPrimaryLanguageModifierCodeSequence"
    },
    "00101000" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "OtherPatientIDs"
    },
    "00101001" : {
        "VR" : "PN",
        "VM" : "1-n",
        "KEYWORD" : "OtherPatientNames"
    },
    "00101002" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OtherPatientIDsSequence"
    },
    "00101005" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "PatientBirthName"
    },
    "00101010" : {
        "VR" : "AS",
        "VM" : "1",
        "KEYWORD" : "PatientAge"
    },
    "00101020" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "PatientSize"
    },
    "00101021" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PatientSizeCodeSequence"
    },
    "00101030" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "PatientWeight"
    },
    "00101040" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PatientAddress"
    },
    "00101050" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "InsurancePlanIdentification"
    },
    "00101060" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "PatientMotherBirthName"
    },
    "00101080" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "MilitaryRank"
    },
    "00101081" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "BranchOfService"
    },
    "00101090" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "MedicalRecordLocator"
    },
    "00102000" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "MedicalAlerts"
    },
    "00102110" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "Allergies"
    },
    "00102150" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "CountryOfResidence"
    },
    "00102152" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "RegionOfResidence"
    },
    "00102154" : {
        "VR" : "SH",
        "VM" : "1-n",
        "KEYWORD" : "PatientTelephoneNumbers"
    },
    "00102160" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "EthnicGroup"
    },
    "00102180" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "Occupation"
    },
    "001021A0" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SmokingStatus"
    },
    "001021B0" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "AdditionalPatientHistory"
    },
    "001021C0" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "PregnancyStatus"
    },
    "001021D0" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "LastMenstrualDate"
    },
    "001021F0" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PatientReligiousPreference"
    },
    "00102201" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PatientSpeciesDescription"
    },
    "00102202" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PatientSpeciesCodeSequence"
    },
    "00102203" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PatientSexNeutered"
    },
    "00102210" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AnatomicalOrientationType"
    },
    "00102292" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PatientBreedDescription"
    },
    "00102293" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PatientBreedCodeSequence"
    },
    "00102294" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BreedRegistrationSequence"
    },
    "00102295" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "BreedRegistrationNumber"
    },
    "00102296" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BreedRegistryCodeSequence"
    },
    "00102297" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "ResponsiblePerson"
    },
    "00102298" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ResponsiblePersonRole"
    },
    "00102299" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ResponsibleOrganization"
    },
    "00104000" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "PatientComments"
    },
    "00109431" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "ExaminedBodyThickness"
    },
    "00120010" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ClinicalTrialSponsorName"
    },
    "00120020" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ClinicalTrialProtocolID"
    },
    "00120021" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ClinicalTrialProtocolName"
    },
    "00120030" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ClinicalTrialSiteID"
    },
    "00120031" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ClinicalTrialSiteName"
    },
    "00120040" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ClinicalTrialSubjectID"
    },
    "00120042" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ClinicalTrialSubjectReadingID"
    },
    "00120050" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ClinicalTrialTimePointID"
    },
    "00120051" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "ClinicalTrialTimePointDescription"
    },
    "00120060" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ClinicalTrialCoordinatingCenterName"
    },
    "00120062" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PatientIdentityRemoved"
    },
    "00120063" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "DeidentificationMethod"
    },
    "00120064" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DeidentificationMethodCodeSequence"
    },
    "00120071" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ClinicalTrialSeriesID"
    },
    "00120072" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ClinicalTrialSeriesDescription"
    },
    "00120081" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ClinicalTrialProtocolEthicsCommitteeName"
    },
    "00120082" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ClinicalTrialProtocolEthicsCommitteeApprovalNumber"
    },
    "00120083" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ConsentForClinicalTrialUseSequence"
    },
    "00120084" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DistributionType"
    },
    "00120085" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ConsentForDistributionFlag"
    },
    "00140023" : {
        "VR" : "ST",
        "VM" : "1-n",
        "KEYWORD" : "CADFileFormat"
    },
    "00140024" : {
        "VR" : "ST",
        "VM" : "1-n",
        "KEYWORD" : "ComponentReferenceSystem"
    },
    "00140025" : {
        "VR" : "ST",
        "VM" : "1-n",
        "KEYWORD" : "ComponentManufacturingProcedure"
    },
    "00140028" : {
        "VR" : "ST",
        "VM" : "1-n",
        "KEYWORD" : "ComponentManufacturer"
    },
    "00140030" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "MaterialThickness"
    },
    "00140032" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "MaterialPipeDiameter"
    },
    "00140034" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "MaterialIsolationDiameter"
    },
    "00140042" : {
        "VR" : "ST",
        "VM" : "1-n",
        "KEYWORD" : "MaterialGrade"
    },
    "00140044" : {
        "VR" : "ST",
        "VM" : "1-n",
        "KEYWORD" : "MaterialPropertiesFileID"
    },
    "00140045" : {
        "VR" : "ST",
        "VM" : "1-n",
        "KEYWORD" : "MaterialPropertiesFileFormat"
    },
    "00140046" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "MaterialNotes"
    },
    "00140050" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ComponentShape"
    },
    "00140052" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CurvatureType"
    },
    "00140054" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "OuterDiameter"
    },
    "00140056" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "InnerDiameter"
    },
    "00141010" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "ActualEnvironmentalConditions"
    },
    "00141020" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "ExpiryDate"
    },
    "00141040" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "EnvironmentalConditions"
    },
    "00142002" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "EvaluatorSequence"
    },
    "00142004" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "EvaluatorNumber"
    },
    "00142006" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "EvaluatorName"
    },
    "00142008" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "EvaluationAttempt"
    },
    "00142012" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IndicationSequence"
    },
    "00142014" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "IndicationNumber "
    },
    "00142016" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "IndicationLabel"
    },
    "00142018" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "IndicationDescription"
    },
    "0014201A" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "IndicationType"
    },
    "0014201C" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "IndicationDisposition"
    },
    "0014201E" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IndicationROISequence"
    },
    "00142030" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IndicationPhysicalPropertySequence"
    },
    "00142032" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "PropertyLabel"
    },
    "00142202" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "CoordinateSystemNumberOfAxes "
    },
    "00142204" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CoordinateSystemAxesSequence"
    },
    "00142206" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "CoordinateSystemAxisDescription"
    },
    "00142208" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CoordinateSystemDataSetMapping"
    },
    "0014220A" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "CoordinateSystemAxisNumber"
    },
    "0014220C" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CoordinateSystemAxisType"
    },
    "0014220E" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CoordinateSystemAxisUnits"
    },
    "00142210" : {
        "VR" : "OB",
        "VM" : "1",
        "KEYWORD" : "CoordinateSystemAxisValues"
    },
    "00142220" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CoordinateSystemTransformSequence"
    },
    "00142222" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "TransformDescription"
    },
    "00142224" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "TransformNumberOfAxes"
    },
    "00142226" : {
        "VR" : "IS",
        "VM" : "1-n",
        "KEYWORD" : "TransformOrderOfAxes"
    },
    "00142228" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TransformedAxisUnits"
    },
    "0014222A" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "CoordinateSystemTransformRotationAndScaleMatrix"
    },
    "0014222C" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "CoordinateSystemTransformTranslationMatrix"
    },
    "00143011" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "InternalDetectorFrameTime"
    },
    "00143012" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "NumberOfFramesIntegrated"
    },
    "00143020" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DetectorTemperatureSequence"
    },
    "00143022" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SensorName"
    },
    "00143024" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "HorizontalOffsetOfSensor"
    },
    "00143026" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "VerticalOffsetOfSensor"
    },
    "00143028" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SensorTemperature"
    },
    "00143040" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DarkCurrentSequence"
    },
    "00143050" : {
        "VR" : "OB|OW",
        "VM" : "1",
        "KEYWORD" : "DarkCurrentCounts"
    },
    "00143060" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "GainCorrectionReferenceSequence"
    },
    "00143070" : {
        "VR" : "OB|OW",
        "VM" : "1",
        "KEYWORD" : "AirCounts"
    },
    "00143071" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "KVUsedInGainCalibration"
    },
    "00143072" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "MAUsedInGainCalibration"
    },
    "00143073" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "NumberOfFramesUsedForIntegration"
    },
    "00143074" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "FilterMaterialUsedInGainCalibration"
    },
    "00143075" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "FilterThicknessUsedInGainCalibration"
    },
    "00143076" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "DateOfGainCalibration"
    },
    "00143077" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "TimeOfGainCalibration"
    },
    "00143080" : {
        "VR" : "OB",
        "VM" : "1",
        "KEYWORD" : "BadPixelImage"
    },
    "00143099" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "CalibrationNotes"
    },
    "00144002" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PulserEquipmentSequence"
    },
    "00144004" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PulserType"
    },
    "00144006" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "PulserNotes"
    },
    "00144008" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReceiverEquipmentSequence"
    },
    "0014400A" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AmplifierType"
    },
    "0014400C" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "ReceiverNotes"
    },
    "0014400E" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PreAmplifierEquipmentSequence"
    },
    "0014400F" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "PreAmplifierNotes"
    },
    "00144010" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TransmitTransducerSequence"
    },
    "00144011" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReceiveTransducerSequence"
    },
    "00144012" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfElements"
    },
    "00144013" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ElementShape"
    },
    "00144014" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ElementDimensionA"
    },
    "00144015" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ElementDimensionB"
    },
    "00144016" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ElementPitch"
    },
    "00144017" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "MeasuredBeamDimensionA"
    },
    "00144018" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "MeasuredBeamDimensionB"
    },
    "00144019" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "LocationOfMeasuredBeamDiameter"
    },
    "0014401A" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "NominalFrequency"
    },
    "0014401B" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "MeasuredCenterFrequency"
    },
    "0014401C" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "MeasuredBandwidth"
    },
    "00144020" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PulserSettingsSequence"
    },
    "00144022" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "PulseWidth"
    },
    "00144024" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ExcitationFrequency"
    },
    "00144026" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ModulationType"
    },
    "00144028" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "Damping"
    },
    "00144030" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReceiverSettingsSequence"
    },
    "00144031" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "AcquiredSoundpathLength"
    },
    "00144032" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AcquisitionCompressionType"
    },
    "00144033" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "AcquisitionSampleSize"
    },
    "00144034" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "RectifierSmoothing"
    },
    "00144035" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DACSequence"
    },
    "00144036" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DACType"
    },
    "00144038" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "DACGainPoints"
    },
    "0014403A" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "DACTimePoints"
    },
    "0014403C" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "DACAmplitude"
    },
    "00144040" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PreAmplifierSettingsSequence"
    },
    "00144050" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TransmitTransducerSettingsSequence"
    },
    "00144051" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReceiveTransducerSettingsSequence"
    },
    "00144052" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "IncidentAngle"
    },
    "00144054" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "CouplingTechnique"
    },
    "00144056" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "CouplingMedium"
    },
    "00144057" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "CouplingVelocity"
    },
    "00144058" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "CrystalCenterLocationX"
    },
    "00144059" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "CrystalCenterLocationZ"
    },
    "0014405A" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SoundPathLength"
    },
    "0014405C" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "DelayLawIdentifier"
    },
    "00144060" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "GateSettingsSequence"
    },
    "00144062" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "GateThreshold"
    },
    "00144064" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "VelocityOfSound"
    },
    "00144070" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CalibrationSettingsSequence"
    },
    "00144072" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "CalibrationProcedure"
    },
    "00144074" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "ProcedureVersion"
    },
    "00144076" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "ProcedureCreationDate"
    },
    "00144078" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "ProcedureExpirationDate"
    },
    "0014407A" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "ProcedureLastModifiedDate"
    },
    "0014407C" : {
        "VR" : "TM",
        "VM" : "1-n",
        "KEYWORD" : "CalibrationTime"
    },
    "0014407E" : {
        "VR" : "DA",
        "VM" : "1-n",
        "KEYWORD" : "CalibrationDate"
    },
    "00145002" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "LINACEnergy"
    },
    "00145004" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "LINACOutput"
    },
    "00180010" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ContrastBolusAgent"
    },
    "00180012" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ContrastBolusAgentSequence"
    },
    "00180014" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ContrastBolusAdministrationRouteSequence"
    },
    "00180015" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BodyPartExamined"
    },
    "00180020" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "ScanningSequence"
    },
    "00180021" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "SequenceVariant"
    },
    "00180022" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "ScanOptions"
    },
    "00180023" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "MRAcquisitionType"
    },
    "00180024" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "SequenceName"
    },
    "00180025" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AngioFlag"
    },
    "00180026" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "InterventionDrugInformationSequence"
    },
    "00180027" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "InterventionDrugStopTime"
    },
    "00180028" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "InterventionDrugDose"
    },
    "00180029" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "InterventionDrugCodeSequence"
    },
    "0018002A" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AdditionalDrugSequence"
    },
    "00180030" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "Radionuclide"
    },
    "00180031" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "Radiopharmaceutical"
    },
    "00180032" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "EnergyWindowCenterline"
    },
    "00180033" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "EnergyWindowTotalWidth"
    },
    "00180034" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "InterventionDrugName"
    },
    "00180035" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "InterventionDrugStartTime"
    },
    "00180036" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "InterventionSequence"
    },
    "00180037" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TherapyType"
    },
    "00180038" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "InterventionStatus"
    },
    "00180039" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TherapyDescription"
    },
    "0018003A" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "InterventionDescription"
    },
    "00180040" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "CineRate"
    },
    "00180042" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "InitialCineRunState"
    },
    "00180050" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SliceThickness"
    },
    "00180060" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "KVP"
    },
    "00180070" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "CountsAccumulated"
    },
    "00180071" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AcquisitionTerminationCondition"
    },
    "00180072" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "EffectiveDuration"
    },
    "00180073" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AcquisitionStartCondition"
    },
    "00180074" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "AcquisitionStartConditionData"
    },
    "00180075" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "AcquisitionTerminationConditionData"
    },
    "00180080" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "RepetitionTime"
    },
    "00180081" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "EchoTime"
    },
    "00180082" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "InversionTime"
    },
    "00180083" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "NumberOfAverages"
    },
    "00180084" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ImagingFrequency"
    },
    "00180085" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "ImagedNucleus"
    },
    "00180086" : {
        "VR" : "IS",
        "VM" : "1-n",
        "KEYWORD" : "EchoNumbers"
    },
    "00180087" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "MagneticFieldStrength"
    },
    "00180088" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SpacingBetweenSlices"
    },
    "00180089" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfPhaseEncodingSteps"
    },
    "00180090" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DataCollectionDiameter"
    },
    "00180091" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "EchoTrainLength"
    },
    "00180093" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "PercentSampling"
    },
    "00180094" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "PercentPhaseFieldOfView"
    },
    "00180095" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "PixelBandwidth"
    },
    "00181000" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "DeviceSerialNumber"
    },
    "00181002" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "DeviceUID"
    },
    "00181003" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "DeviceID"
    },
    "00181004" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PlateID"
    },
    "00181005" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "GeneratorID"
    },
    "00181006" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "GridID"
    },
    "00181007" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "CassetteID"
    },
    "00181008" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "GantryID"
    },
    "00181010" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SecondaryCaptureDeviceID"
    },
    "00181011" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "HardcopyCreationDeviceID"
    },
    "00181012" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "DateOfSecondaryCapture"
    },
    "00181014" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "TimeOfSecondaryCapture"
    },
    "00181016" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SecondaryCaptureDeviceManufacturer"
    },
    "00181017" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "HardcopyDeviceManufacturer"
    },
    "00181018" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SecondaryCaptureDeviceManufacturerModelName"
    },
    "00181019" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "SecondaryCaptureDeviceSoftwareVersions"
    },
    "0018101A" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "HardcopyDeviceSoftwareVersion"
    },
    "0018101B" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "HardcopyDeviceManufacturerModelName"
    },
    "00181020" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "SoftwareVersions"
    },
    "00181022" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "VideoImageFormatAcquired"
    },
    "00181023" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "DigitalImageFormatAcquired"
    },
    "00181030" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ProtocolName"
    },
    "00181040" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ContrastBolusRoute"
    },
    "00181041" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ContrastBolusVolume"
    },
    "00181042" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "ContrastBolusStartTime"
    },
    "00181043" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "ContrastBolusStopTime"
    },
    "00181044" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ContrastBolusTotalDose"
    },
    "00181045" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "SyringeCounts"
    },
    "00181046" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "ContrastFlowRate"
    },
    "00181047" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "ContrastFlowDuration"
    },
    "00181048" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ContrastBolusIngredient"
    },
    "00181049" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ContrastBolusIngredientConcentration"
    },
    "00181050" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SpatialResolution"
    },
    "00181060" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TriggerTime"
    },
    "00181061" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "TriggerSourceOrType"
    },
    "00181062" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NominalInterval"
    },
    "00181063" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "FrameTime"
    },
    "00181064" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "CardiacFramingType"
    },
    "00181065" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "FrameTimeVector"
    },
    "00181066" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "FrameDelay"
    },
    "00181067" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ImageTriggerDelay"
    },
    "00181068" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "MultiplexGroupTimeOffset"
    },
    "00181069" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TriggerTimeOffset"
    },
    "0018106A" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SynchronizationTrigger"
    },
    "0018106C" : {
        "VR" : "US",
        "VM" : "2",
        "KEYWORD" : "SynchronizationChannel"
    },
    "0018106E" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "TriggerSamplePosition"
    },
    "00181070" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "RadiopharmaceuticalRoute"
    },
    "00181071" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "RadiopharmaceuticalVolume"
    },
    "00181072" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "RadiopharmaceuticalStartTime"
    },
    "00181073" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "RadiopharmaceuticalStopTime"
    },
    "00181074" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "RadionuclideTotalDose"
    },
    "00181075" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "RadionuclideHalfLife"
    },
    "00181076" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "RadionuclidePositronFraction"
    },
    "00181077" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "RadiopharmaceuticalSpecificActivity"
    },
    "00181078" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "RadiopharmaceuticalStartDateTime"
    },
    "00181079" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "RadiopharmaceuticalStopDateTime"
    },
    "00181080" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BeatRejectionFlag"
    },
    "00181081" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "LowRRValue"
    },
    "00181082" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "HighRRValue"
    },
    "00181083" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "IntervalsAcquired"
    },
    "00181084" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "IntervalsRejected"
    },
    "00181085" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PVCRejection"
    },
    "00181086" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "SkipBeats"
    },
    "00181088" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "HeartRate"
    },
    "00181090" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "CardiacNumberOfImages"
    },
    "00181094" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "TriggerWindow"
    },
    "00181100" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ReconstructionDiameter"
    },
    "00181110" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DistanceSourceToDetector"
    },
    "00181111" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DistanceSourceToPatient"
    },
    "00181114" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "EstimatedRadiographicMagnificationFactor"
    },
    "00181120" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "GantryDetectorTilt"
    },
    "00181121" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "GantryDetectorSlew"
    },
    "00181130" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TableHeight"
    },
    "00181131" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TableTraverse"
    },
    "00181134" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TableMotion"
    },
    "00181135" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "TableVerticalIncrement"
    },
    "00181136" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "TableLateralIncrement"
    },
    "00181137" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "TableLongitudinalIncrement"
    },
    "00181138" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TableAngle"
    },
    "0018113A" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TableType"
    },
    "00181140" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RotationDirection"
    },
    "00181141" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "AngularPosition"
    },
    "00181142" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "RadialPosition"
    },
    "00181143" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ScanArc"
    },
    "00181144" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "AngularStep"
    },
    "00181145" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "CenterOfRotationOffset"
    },
    "00181146" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "RotationOffset"
    },
    "00181147" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FieldOfViewShape"
    },
    "00181149" : {
        "VR" : "IS",
        "VM" : "1-2",
        "KEYWORD" : "FieldOfViewDimensions"
    },
    "00181150" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ExposureTime"
    },
    "00181151" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "XRayTubeCurrent"
    },
    "00181152" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "Exposure"
    },
    "00181153" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ExposureInuAs"
    },
    "00181154" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "AveragePulseWidth"
    },
    "00181155" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RadiationSetting"
    },
    "00181156" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RectificationType"
    },
    "0018115A" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RadiationMode"
    },
    "0018115E" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ImageAndFluoroscopyAreaDoseProduct"
    },
    "00181160" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "FilterType"
    },
    "00181161" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "TypeOfFilters"
    },
    "00181162" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "IntensifierSize"
    },
    "00181164" : {
        "VR" : "DS",
        "VM" : "2",
        "KEYWORD" : "ImagerPixelSpacing"
    },
    "00181166" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "Grid"
    },
    "00181170" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "GeneratorPower"
    },
    "00181180" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "CollimatorGridName"
    },
    "00181181" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CollimatorType"
    },
    "00181182" : {
        "VR" : "IS",
        "VM" : "1-2",
        "KEYWORD" : "FocalDistance"
    },
    "00181183" : {
        "VR" : "DS",
        "VM" : "1-2",
        "KEYWORD" : "XFocusCenter"
    },
    "00181184" : {
        "VR" : "DS",
        "VM" : "1-2",
        "KEYWORD" : "YFocusCenter"
    },
    "00181190" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "FocalSpots"
    },
    "00181191" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AnodeTargetMaterial"
    },
    "001811A0" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "BodyPartThickness"
    },
    "001811A2" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "CompressionForce"
    },
    "00181200" : {
        "VR" : "DA",
        "VM" : "1-n",
        "KEYWORD" : "DateOfLastCalibration"
    },
    "00181201" : {
        "VR" : "TM",
        "VM" : "1-n",
        "KEYWORD" : "TimeOfLastCalibration"
    },
    "00181210" : {
        "VR" : "SH",
        "VM" : "1-n",
        "KEYWORD" : "ConvolutionKernel"
    },
    "00181240" : {
        "VR" : "IS",
        "VM" : "1-n",
        "KEYWORD" : "UpperLowerPixelValues"
    },
    "00181242" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ActualFrameDuration"
    },
    "00181243" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "CountRate"
    },
    "00181244" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "PreferredPlaybackSequencing"
    },
    "00181250" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "ReceiveCoilName"
    },
    "00181251" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "TransmitCoilName"
    },
    "00181260" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "PlateType"
    },
    "00181261" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PhosphorType"
    },
    "00181300" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ScanVelocity"
    },
    "00181301" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "WholeBodyTechnique"
    },
    "00181302" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ScanLength"
    },
    "00181310" : {
        "VR" : "US",
        "VM" : "4",
        "KEYWORD" : "AcquisitionMatrix"
    },
    "00181312" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "InPlanePhaseEncodingDirection"
    },
    "00181314" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "FlipAngle"
    },
    "00181315" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "VariableFlipAngleFlag"
    },
    "00181316" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SAR"
    },
    "00181318" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "dBdt"
    },
    "00181400" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "AcquisitionDeviceProcessingDescription"
    },
    "00181401" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "AcquisitionDeviceProcessingCode"
    },
    "00181402" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CassetteOrientation"
    },
    "00181403" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CassetteSize"
    },
    "00181404" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ExposuresOnPlate"
    },
    "00181405" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "RelativeXRayExposure"
    },
    "00181411" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ExposureIndex"
    },
    "00181412" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TargetExposureIndex"
    },
    "00181413" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DeviationIndex"
    },
    "00181450" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ColumnAngulation"
    },
    "00181460" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TomoLayerHeight"
    },
    "00181470" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TomoAngle"
    },
    "00181480" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TomoTime"
    },
    "00181490" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TomoType"
    },
    "00181491" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TomoClass"
    },
    "00181495" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfTomosynthesisSourceImages"
    },
    "00181500" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PositionerMotion"
    },
    "00181508" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PositionerType"
    },
    "00181510" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "PositionerPrimaryAngle"
    },
    "00181511" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "PositionerSecondaryAngle"
    },
    "00181520" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "PositionerPrimaryAngleIncrement"
    },
    "00181521" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "PositionerSecondaryAngleIncrement"
    },
    "00181530" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DetectorPrimaryAngle"
    },
    "00181531" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DetectorSecondaryAngle"
    },
    "00181600" : {
        "VR" : "CS",
        "VM" : "1-3",
        "KEYWORD" : "ShutterShape"
    },
    "00181602" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ShutterLeftVerticalEdge"
    },
    "00181604" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ShutterRightVerticalEdge"
    },
    "00181606" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ShutterUpperHorizontalEdge"
    },
    "00181608" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ShutterLowerHorizontalEdge"
    },
    "00181610" : {
        "VR" : "IS",
        "VM" : "2",
        "KEYWORD" : "CenterOfCircularShutter"
    },
    "00181612" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "RadiusOfCircularShutter"
    },
    "00181620" : {
        "VR" : "IS",
        "VM" : "2-2n",
        "KEYWORD" : "VerticesOfThePolygonalShutter"
    },
    "00181622" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ShutterPresentationValue"
    },
    "00181623" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ShutterOverlayGroup"
    },
    "00181624" : {
        "VR" : "US",
        "VM" : "3",
        "KEYWORD" : "ShutterPresentationColorCIELabValue"
    },
    "00181700" : {
        "VR" : "CS",
        "VM" : "1-3",
        "KEYWORD" : "CollimatorShape"
    },
    "00181702" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "CollimatorLeftVerticalEdge"
    },
    "00181704" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "CollimatorRightVerticalEdge"
    },
    "00181706" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "CollimatorUpperHorizontalEdge"
    },
    "00181708" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "CollimatorLowerHorizontalEdge"
    },
    "00181710" : {
        "VR" : "IS",
        "VM" : "2",
        "KEYWORD" : "CenterOfCircularCollimator"
    },
    "00181712" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "RadiusOfCircularCollimator"
    },
    "00181720" : {
        "VR" : "IS",
        "VM" : "2-2n",
        "KEYWORD" : "VerticesOfThePolygonalCollimator"
    },
    "00181800" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AcquisitionTimeSynchronized"
    },
    "00181801" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "TimeSource"
    },
    "00181802" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TimeDistributionProtocol"
    },
    "00181803" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "NTPSourceAddress"
    },
    "00182001" : {
        "VR" : "IS",
        "VM" : "1-n",
        "KEYWORD" : "PageNumberVector"
    },
    "00182002" : {
        "VR" : "SH",
        "VM" : "1-n",
        "KEYWORD" : "FrameLabelVector"
    },
    "00182003" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "FramePrimaryAngleVector"
    },
    "00182004" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "FrameSecondaryAngleVector"
    },
    "00182005" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "SliceLocationVector"
    },
    "00182006" : {
        "VR" : "SH",
        "VM" : "1-n",
        "KEYWORD" : "DisplayWindowLabelVector"
    },
    "00182010" : {
        "VR" : "DS",
        "VM" : "2",
        "KEYWORD" : "NominalScannedPixelSpacing"
    },
    "00182020" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DigitizingDeviceTransportDirection"
    },
    "00182030" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "RotationOfScannedFilm"
    },
    "00183100" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "IVUSAcquisition"
    },
    "00183101" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "IVUSPullbackRate"
    },
    "00183102" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "IVUSGatedRate"
    },
    "00183103" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "IVUSPullbackStartFrameNumber"
    },
    "00183104" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "IVUSPullbackStopFrameNumber"
    },
    "00183105" : {
        "VR" : "IS",
        "VM" : "1-n",
        "KEYWORD" : "LesionNumber"
    },
    "00184000" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "AcquisitionComments"
    },
    "00185000" : {
        "VR" : "SH",
        "VM" : "1-n",
        "KEYWORD" : "OutputPower"
    },
    "00185010" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "TransducerData"
    },
    "00185012" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "FocusDepth"
    },
    "00185020" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ProcessingFunction"
    },
    "00185021" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PostprocessingFunction"
    },
    "00185022" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "MechanicalIndex"
    },
    "00185024" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "BoneThermalIndex"
    },
    "00185026" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "CranialThermalIndex"
    },
    "00185027" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SoftTissueThermalIndex"
    },
    "00185028" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SoftTissueFocusThermalIndex"
    },
    "00185029" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SoftTissueSurfaceThermalIndex"
    },
    "00185030" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DynamicRange"
    },
    "00185040" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TotalGain"
    },
    "00185050" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "DepthOfScanField"
    },
    "00185100" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PatientPosition"
    },
    "00185101" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ViewPosition"
    },
    "00185104" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ProjectionEponymousNameCodeSequence"
    },
    "00185210" : {
        "VR" : "DS",
        "VM" : "6",
        "KEYWORD" : "ImageTransformationMatrix"
    },
    "00185212" : {
        "VR" : "DS",
        "VM" : "3",
        "KEYWORD" : "ImageTranslationVector"
    },
    "00186000" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "Sensitivity"
    },
    "00186011" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SequenceOfUltrasoundRegions"
    },
    "00186012" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "RegionSpatialFormat"
    },
    "00186014" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "RegionDataType"
    },
    "00186016" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "RegionFlags"
    },
    "00186018" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "RegionLocationMinX0"
    },
    "0018601A" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "RegionLocationMinY0"
    },
    "0018601C" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "RegionLocationMaxX1"
    },
    "0018601E" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "RegionLocationMaxY1"
    },
    "00186020" : {
        "VR" : "SL",
        "VM" : "1",
        "KEYWORD" : "ReferencePixelX0"
    },
    "00186022" : {
        "VR" : "SL",
        "VM" : "1",
        "KEYWORD" : "ReferencePixelY0"
    },
    "00186024" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "PhysicalUnitsXDirection"
    },
    "00186026" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "PhysicalUnitsYDirection"
    },
    "00186028" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ReferencePixelPhysicalValueX"
    },
    "0018602A" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ReferencePixelPhysicalValueY"
    },
    "0018602C" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "PhysicalDeltaX"
    },
    "0018602E" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "PhysicalDeltaY"
    },
    "00186030" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "TransducerFrequency"
    },
    "00186031" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TransducerType"
    },
    "00186032" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "PulseRepetitionFrequency"
    },
    "00186034" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "DopplerCorrectionAngle"
    },
    "00186036" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "SteeringAngle"
    },
    "00186038" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "DopplerSampleVolumeXPositionRetired"
    },
    "00186039" : {
        "VR" : "SL",
        "VM" : "1",
        "KEYWORD" : "DopplerSampleVolumeXPosition"
    },
    "0018603A" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "DopplerSampleVolumeYPositionRetired"
    },
    "0018603B" : {
        "VR" : "SL",
        "VM" : "1",
        "KEYWORD" : "DopplerSampleVolumeYPosition"
    },
    "0018603C" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "TMLinePositionX0Retired"
    },
    "0018603D" : {
        "VR" : "SL",
        "VM" : "1",
        "KEYWORD" : "TMLinePositionX0"
    },
    "0018603E" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "TMLinePositionY0Retired"
    },
    "0018603F" : {
        "VR" : "SL",
        "VM" : "1",
        "KEYWORD" : "TMLinePositionY0"
    },
    "00186040" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "TMLinePositionX1Retired"
    },
    "00186041" : {
        "VR" : "SL",
        "VM" : "1",
        "KEYWORD" : "TMLinePositionX1"
    },
    "00186042" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "TMLinePositionY1Retired"
    },
    "00186043" : {
        "VR" : "SL",
        "VM" : "1",
        "KEYWORD" : "TMLinePositionY1"
    },
    "00186044" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "PixelComponentOrganization"
    },
    "00186046" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "PixelComponentMask"
    },
    "00186048" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "PixelComponentRangeStart"
    },
    "0018604A" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "PixelComponentRangeStop"
    },
    "0018604C" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "PixelComponentPhysicalUnits"
    },
    "0018604E" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "PixelComponentDataType"
    },
    "00186050" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "NumberOfTableBreakPoints"
    },
    "00186052" : {
        "VR" : "UL",
        "VM" : "1-n",
        "KEYWORD" : "TableOfXBreakPoints"
    },
    "00186054" : {
        "VR" : "FD",
        "VM" : "1-n",
        "KEYWORD" : "TableOfYBreakPoints"
    },
    "00186056" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "NumberOfTableEntries"
    },
    "00186058" : {
        "VR" : "UL",
        "VM" : "1-n",
        "KEYWORD" : "TableOfPixelValues"
    },
    "0018605A" : {
        "VR" : "FL",
        "VM" : "1-n",
        "KEYWORD" : "TableOfParameterValues"
    },
    "00186060" : {
        "VR" : "FL",
        "VM" : "1-n",
        "KEYWORD" : "RWaveTimeVector"
    },
    "00187000" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DetectorConditionsNominalFlag"
    },
    "00187001" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DetectorTemperature"
    },
    "00187004" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DetectorType"
    },
    "00187005" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DetectorConfiguration"
    },
    "00187006" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "DetectorDescription"
    },
    "00187008" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "DetectorMode"
    },
    "0018700A" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "DetectorID"
    },
    "0018700C" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "DateOfLastDetectorCalibration"
    },
    "0018700E" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "TimeOfLastDetectorCalibration"
    },
    "00187010" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ExposuresOnDetectorSinceLastCalibration"
    },
    "00187011" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ExposuresOnDetectorSinceManufactured"
    },
    "00187012" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DetectorTimeSinceLastExposure"
    },
    "00187014" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DetectorActiveTime"
    },
    "00187016" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DetectorActivationOffsetFromExposure"
    },
    "0018701A" : {
        "VR" : "DS",
        "VM" : "2",
        "KEYWORD" : "DetectorBinning"
    },
    "00187020" : {
        "VR" : "DS",
        "VM" : "2",
        "KEYWORD" : "DetectorElementPhysicalSize"
    },
    "00187022" : {
        "VR" : "DS",
        "VM" : "2",
        "KEYWORD" : "DetectorElementSpacing"
    },
    "00187024" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DetectorActiveShape"
    },
    "00187026" : {
        "VR" : "DS",
        "VM" : "1-2",
        "KEYWORD" : "DetectorActiveDimensions"
    },
    "00187028" : {
        "VR" : "DS",
        "VM" : "2",
        "KEYWORD" : "DetectorActiveOrigin"
    },
    "0018702A" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "DetectorManufacturerName"
    },
    "0018702B" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "DetectorManufacturerModelName"
    },
    "00187030" : {
        "VR" : "DS",
        "VM" : "2",
        "KEYWORD" : "FieldOfViewOrigin"
    },
    "00187032" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "FieldOfViewRotation"
    },
    "00187034" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FieldOfViewHorizontalFlip"
    },
    "00187036" : {
        "VR" : "FL",
        "VM" : "2",
        "KEYWORD" : "PixelDataAreaOriginRelativeToFOV"
    },
    "00187038" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "PixelDataAreaRotationAngleRelativeToFOV"
    },
    "00187040" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "GridAbsorbingMaterial"
    },
    "00187041" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "GridSpacingMaterial"
    },
    "00187042" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "GridThickness"
    },
    "00187044" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "GridPitch"
    },
    "00187046" : {
        "VR" : "IS",
        "VM" : "2",
        "KEYWORD" : "GridAspectRatio"
    },
    "00187048" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "GridPeriod"
    },
    "0018704C" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "GridFocalDistance"
    },
    "00187050" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "FilterMaterial"
    },
    "00187052" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "FilterThicknessMinimum"
    },
    "00187054" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "FilterThicknessMaximum"
    },
    "00187056" : {
        "VR" : "FL",
        "VM" : "1-n",
        "KEYWORD" : "FilterBeamPathLengthMinimum"
    },
    "00187058" : {
        "VR" : "FL",
        "VM" : "1-n",
        "KEYWORD" : "FilterBeamPathLengthMaximum"
    },
    "00187060" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ExposureControlMode"
    },
    "00187062" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "ExposureControlModeDescription"
    },
    "00187064" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ExposureStatus"
    },
    "00187065" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "PhototimerSetting"
    },
    "00188150" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ExposureTimeInuS"
    },
    "00188151" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "XRayTubeCurrentInuA"
    },
    "00189004" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ContentQualification"
    },
    "00189005" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "PulseSequenceName"
    },
    "00189006" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MRImagingModifierSequence"
    },
    "00189008" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "EchoPulseSequence"
    },
    "00189009" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "InversionRecovery"
    },
    "00189010" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FlowCompensation"
    },
    "00189011" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "MultipleSpinEcho"
    },
    "00189012" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "MultiPlanarExcitation"
    },
    "00189014" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PhaseContrast"
    },
    "00189015" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TimeOfFlightContrast"
    },
    "00189016" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "Spoiling"
    },
    "00189017" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SteadyStatePulseSequence"
    },
    "00189018" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "EchoPlanarPulseSequence"
    },
    "00189019" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TagAngleFirstAxis"
    },
    "00189020" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "MagnetizationTransfer"
    },
    "00189021" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "T2Preparation"
    },
    "00189022" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BloodSignalNulling"
    },
    "00189024" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SaturationRecovery"
    },
    "00189025" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SpectrallySelectedSuppression"
    },
    "00189026" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SpectrallySelectedExcitation"
    },
    "00189027" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SpatialPresaturation"
    },
    "00189028" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "Tagging"
    },
    "00189029" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OversamplingPhase"
    },
    "00189030" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TagSpacingFirstDimension"
    },
    "00189032" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "GeometryOfKSpaceTraversal"
    },
    "00189033" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SegmentedKSpaceTraversal"
    },
    "00189034" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RectilinearPhaseEncodeReordering"
    },
    "00189035" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TagThickness"
    },
    "00189036" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PartialFourierDirection"
    },
    "00189037" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CardiacSynchronizationTechnique"
    },
    "00189041" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ReceiveCoilManufacturerName"
    },
    "00189042" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MRReceiveCoilSequence"
    },
    "00189043" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ReceiveCoilType"
    },
    "00189044" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "QuadratureReceiveCoil"
    },
    "00189045" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MultiCoilDefinitionSequence"
    },
    "00189046" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "MultiCoilConfiguration"
    },
    "00189047" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "MultiCoilElementName"
    },
    "00189048" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "MultiCoilElementUsed"
    },
    "00189049" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MRTransmitCoilSequence"
    },
    "00189050" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "TransmitCoilManufacturerName"
    },
    "00189051" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TransmitCoilType"
    },
    "00189052" : {
        "VR" : "FD",
        "VM" : "1-2",
        "KEYWORD" : "SpectralWidth"
    },
    "00189053" : {
        "VR" : "FD",
        "VM" : "1-2",
        "KEYWORD" : "ChemicalShiftReference"
    },
    "00189054" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "VolumeLocalizationTechnique"
    },
    "00189058" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "MRAcquisitionFrequencyEncodingSteps"
    },
    "00189059" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "Decoupling"
    },
    "00189060" : {
        "VR" : "CS",
        "VM" : "1-2",
        "KEYWORD" : "DecoupledNucleus"
    },
    "00189061" : {
        "VR" : "FD",
        "VM" : "1-2",
        "KEYWORD" : "DecouplingFrequency"
    },
    "00189062" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DecouplingMethod"
    },
    "00189063" : {
        "VR" : "FD",
        "VM" : "1-2",
        "KEYWORD" : "DecouplingChemicalShiftReference"
    },
    "00189064" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "KSpaceFiltering"
    },
    "00189065" : {
        "VR" : "CS",
        "VM" : "1-2",
        "KEYWORD" : "TimeDomainFiltering"
    },
    "00189066" : {
        "VR" : "US",
        "VM" : "1-2",
        "KEYWORD" : "NumberOfZeroFills"
    },
    "00189067" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BaselineCorrection"
    },
    "00189069" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ParallelReductionFactorInPlane"
    },
    "00189070" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "CardiacRRIntervalSpecified"
    },
    "00189073" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "AcquisitionDuration"
    },
    "00189074" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "FrameAcquisitionDateTime"
    },
    "00189075" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DiffusionDirectionality"
    },
    "00189076" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DiffusionGradientDirectionSequence"
    },
    "00189077" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ParallelAcquisition"
    },
    "00189078" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ParallelAcquisitionTechnique"
    },
    "00189079" : {
        "VR" : "FD",
        "VM" : "1-n",
        "KEYWORD" : "InversionTimes"
    },
    "00189080" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "MetaboliteMapDescription"
    },
    "00189081" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PartialFourier"
    },
    "00189082" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "EffectiveEchoTime"
    },
    "00189083" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MetaboliteMapCodeSequence"
    },
    "00189084" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ChemicalShiftSequence"
    },
    "00189085" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CardiacSignalSource"
    },
    "00189087" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "DiffusionBValue"
    },
    "00189089" : {
        "VR" : "FD",
        "VM" : "3",
        "KEYWORD" : "DiffusionGradientOrientation"
    },
    "00189090" : {
        "VR" : "FD",
        "VM" : "3",
        "KEYWORD" : "VelocityEncodingDirection"
    },
    "00189091" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "VelocityEncodingMinimumValue"
    },
    "00189092" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "VelocityEncodingAcquisitionSequence"
    },
    "00189093" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfKSpaceTrajectories"
    },
    "00189094" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CoverageOfKSpace"
    },
    "00189095" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "SpectroscopyAcquisitionPhaseRows"
    },
    "00189096" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ParallelReductionFactorInPlaneRetired"
    },
    "00189098" : {
        "VR" : "FD",
        "VM" : "1-2",
        "KEYWORD" : "TransmitterFrequency"
    },
    "00189100" : {
        "VR" : "CS",
        "VM" : "1-2",
        "KEYWORD" : "ResonantNucleus"
    },
    "00189101" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FrequencyCorrection"
    },
    "00189103" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MRSpectroscopyFOVGeometrySequence"
    },
    "00189104" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "SlabThickness"
    },
    "00189105" : {
        "VR" : "FD",
        "VM" : "3",
        "KEYWORD" : "SlabOrientation"
    },
    "00189106" : {
        "VR" : "FD",
        "VM" : "3",
        "KEYWORD" : "MidSlabPosition"
    },
    "00189107" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MRSpatialSaturationSequence"
    },
    "00189112" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MRTimingAndRelatedParametersSequence"
    },
    "00189114" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MREchoSequence"
    },
    "00189115" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MRModifierSequence"
    },
    "00189117" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MRDiffusionSequence"
    },
    "00189118" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CardiacSynchronizationSequence"
    },
    "00189119" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MRAveragesSequence"
    },
    "00189125" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MRFOVGeometrySequence"
    },
    "00189126" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "VolumeLocalizationSequence"
    },
    "00189127" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "SpectroscopyAcquisitionDataColumns"
    },
    "00189147" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DiffusionAnisotropyType"
    },
    "00189151" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "FrameReferenceDateTime"
    },
    "00189152" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MRMetaboliteMapSequence"
    },
    "00189155" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ParallelReductionFactorOutOfPlane"
    },
    "00189159" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "SpectroscopyAcquisitionOutOfPlanePhaseSteps"
    },
    "00189166" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BulkMotionStatus"
    },
    "00189168" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ParallelReductionFactorSecondInPlane"
    },
    "00189169" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CardiacBeatRejectionTechnique"
    },
    "00189170" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RespiratoryMotionCompensationTechnique"
    },
    "00189171" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RespiratorySignalSource"
    },
    "00189172" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BulkMotionCompensationTechnique"
    },
    "00189173" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BulkMotionSignalSource"
    },
    "00189174" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ApplicableSafetyStandardAgency"
    },
    "00189175" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ApplicableSafetyStandardDescription"
    },
    "00189176" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OperatingModeSequence"
    },
    "00189177" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OperatingModeType"
    },
    "00189178" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OperatingMode"
    },
    "00189179" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SpecificAbsorptionRateDefinition"
    },
    "00189180" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "GradientOutputType"
    },
    "00189181" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "SpecificAbsorptionRateValue"
    },
    "00189182" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "GradientOutput"
    },
    "00189183" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FlowCompensationDirection"
    },
    "00189184" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TaggingDelay"
    },
    "00189185" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "RespiratoryMotionCompensationTechniqueDescription"
    },
    "00189186" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "RespiratorySignalSourceID"
    },
    "00189195" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ChemicalShiftMinimumIntegrationLimitInHz"
    },
    "00189196" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ChemicalShiftMaximumIntegrationLimitInHz"
    },
    "00189197" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MRVelocityEncodingSequence"
    },
    "00189198" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FirstOrderPhaseCorrection"
    },
    "00189199" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "WaterReferencedPhaseCorrection"
    },
    "00189200" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "MRSpectroscopyAcquisitionType"
    },
    "00189214" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RespiratoryCyclePosition"
    },
    "00189217" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "VelocityEncodingMaximumValue"
    },
    "00189218" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TagSpacingSecondDimension"
    },
    "00189219" : {
        "VR" : "SS",
        "VM" : "1",
        "KEYWORD" : "TagAngleSecondAxis"
    },
    "00189220" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "FrameAcquisitionDuration"
    },
    "00189226" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MRImageFrameTypeSequence"
    },
    "00189227" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MRSpectroscopyFrameTypeSequence"
    },
    "00189231" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "MRAcquisitionPhaseEncodingStepsInPlane"
    },
    "00189232" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "MRAcquisitionPhaseEncodingStepsOutOfPlane"
    },
    "00189234" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "SpectroscopyAcquisitionPhaseColumns"
    },
    "00189236" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CardiacCyclePosition"
    },
    "00189239" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SpecificAbsorptionRateSequence"
    },
    "00189240" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "RFEchoTrainLength"
    },
    "00189241" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "GradientEchoTrainLength"
    },
    "00189250" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ArterialSpinLabelingContrast"
    },
    "00189251" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MRArterialSpinLabelingSequence"
    },
    "00189252" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ASLTechniqueDescription"
    },
    "00189253" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ASLSlabNumber"
    },
    "00189254" : {
        "VR" : "FD",
        "VM" : "1 ",
        "KEYWORD" : "ASLSlabThickness"
    },
    "00189255" : {
        "VR" : "FD",
        "VM" : "3 ",
        "KEYWORD" : "ASLSlabOrientation"
    },
    "00189256" : {
        "VR" : "FD",
        "VM" : "3",
        "KEYWORD" : "ASLMidSlabPosition"
    },
    "00189257" : {
        "VR" : "CS",
        "VM" : "1 ",
        "KEYWORD" : "ASLContext"
    },
    "00189258" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "ASLPulseTrainDuration"
    },
    "00189259" : {
        "VR" : "CS",
        "VM" : "1 ",
        "KEYWORD" : "ASLCrusherFlag"
    },
    "0018925A" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ASLCrusherFlow"
    },
    "0018925B" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ASLCrusherDescription"
    },
    "0018925C" : {
        "VR" : "CS",
        "VM" : "1 ",
        "KEYWORD" : "ASLBolusCutoffFlag"
    },
    "0018925D" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ASLBolusCutoffTimingSequence"
    },
    "0018925E" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ASLBolusCutoffTechnique"
    },
    "0018925F" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "ASLBolusCutoffDelayTime"
    },
    "00189260" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ASLSlabSequence"
    },
    "00189295" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ChemicalShiftMinimumIntegrationLimitInppm"
    },
    "00189296" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ChemicalShiftMaximumIntegrationLimitInppm"
    },
    "00189301" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CTAcquisitionTypeSequence"
    },
    "00189302" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AcquisitionType"
    },
    "00189303" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TubeAngle"
    },
    "00189304" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CTAcquisitionDetailsSequence"
    },
    "00189305" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "RevolutionTime"
    },
    "00189306" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "SingleCollimationWidth"
    },
    "00189307" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TotalCollimationWidth"
    },
    "00189308" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CTTableDynamicsSequence"
    },
    "00189309" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TableSpeed"
    },
    "00189310" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TableFeedPerRotation"
    },
    "00189311" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "SpiralPitchFactor"
    },
    "00189312" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CTGeometrySequence"
    },
    "00189313" : {
        "VR" : "FD",
        "VM" : "3",
        "KEYWORD" : "DataCollectionCenterPatient"
    },
    "00189314" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CTReconstructionSequence"
    },
    "00189315" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ReconstructionAlgorithm"
    },
    "00189316" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ConvolutionKernelGroup"
    },
    "00189317" : {
        "VR" : "FD",
        "VM" : "2",
        "KEYWORD" : "ReconstructionFieldOfView"
    },
    "00189318" : {
        "VR" : "FD",
        "VM" : "3",
        "KEYWORD" : "ReconstructionTargetCenterPatient"
    },
    "00189319" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ReconstructionAngle"
    },
    "00189320" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "ImageFilter"
    },
    "00189321" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CTExposureSequence"
    },
    "00189322" : {
        "VR" : "FD",
        "VM" : "2",
        "KEYWORD" : "ReconstructionPixelSpacing"
    },
    "00189323" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ExposureModulationType"
    },
    "00189324" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "EstimatedDoseSaving"
    },
    "00189325" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CTXRayDetailsSequence"
    },
    "00189326" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CTPositionSequence"
    },
    "00189327" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TablePosition"
    },
    "00189328" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ExposureTimeInms"
    },
    "00189329" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CTImageFrameTypeSequence"
    },
    "00189330" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "XRayTubeCurrentInmA"
    },
    "00189332" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ExposureInmAs"
    },
    "00189333" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ConstantVolumeFlag"
    },
    "00189334" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FluoroscopyFlag"
    },
    "00189335" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "DistanceSourceToDataCollectionCenter"
    },
    "00189337" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ContrastBolusAgentNumber"
    },
    "00189338" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ContrastBolusIngredientCodeSequence"
    },
    "00189340" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ContrastAdministrationProfileSequence"
    },
    "00189341" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ContrastBolusUsageSequence"
    },
    "00189342" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ContrastBolusAgentAdministered"
    },
    "00189343" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ContrastBolusAgentDetected"
    },
    "00189344" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ContrastBolusAgentPhase"
    },
    "00189345" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "CTDIvol"
    },
    "00189346" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CTDIPhantomTypeCodeSequence"
    },
    "00189351" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "CalciumScoringMassFactorPatient"
    },
    "00189352" : {
        "VR" : "FL",
        "VM" : "3",
        "KEYWORD" : "CalciumScoringMassFactorDevice"
    },
    "00189353" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "EnergyWeightingFactor"
    },
    "00189360" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CTAdditionalXRaySourceSequence"
    },
    "00189401" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ProjectionPixelCalibrationSequence"
    },
    "00189402" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "DistanceSourceToIsocenter"
    },
    "00189403" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "DistanceObjectToTableTop"
    },
    "00189404" : {
        "VR" : "FL",
        "VM" : "2",
        "KEYWORD" : "ObjectPixelSpacingInCenterOfBeam"
    },
    "00189405" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PositionerPositionSequence"
    },
    "00189406" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TablePositionSequence"
    },
    "00189407" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CollimatorShapeSequence"
    },
    "00189410" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PlanesInAcquisition"
    },
    "00189412" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "XAXRFFrameCharacteristicsSequence"
    },
    "00189417" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FrameAcquisitionSequence"
    },
    "00189420" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "XRayReceptorType"
    },
    "00189423" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "AcquisitionProtocolName"
    },
    "00189424" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "AcquisitionProtocolDescription"
    },
    "00189425" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ContrastBolusIngredientOpaque"
    },
    "00189426" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "DistanceReceptorPlaneToDetectorHousing"
    },
    "00189427" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "IntensifierActiveShape"
    },
    "00189428" : {
        "VR" : "FL",
        "VM" : "1-2",
        "KEYWORD" : "IntensifierActiveDimensions"
    },
    "00189429" : {
        "VR" : "FL",
        "VM" : "2",
        "KEYWORD" : "PhysicalDetectorSize"
    },
    "00189430" : {
        "VR" : "FL",
        "VM" : "2",
        "KEYWORD" : "PositionOfIsocenterProjection"
    },
    "00189432" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FieldOfViewSequence"
    },
    "00189433" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "FieldOfViewDescription"
    },
    "00189434" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ExposureControlSensingRegionsSequence"
    },
    "00189435" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ExposureControlSensingRegionShape"
    },
    "00189436" : {
        "VR" : "SS",
        "VM" : "1",
        "KEYWORD" : "ExposureControlSensingRegionLeftVerticalEdge"
    },
    "00189437" : {
        "VR" : "SS",
        "VM" : "1",
        "KEYWORD" : "ExposureControlSensingRegionRightVerticalEdge"
    },
    "00189438" : {
        "VR" : "SS",
        "VM" : "1",
        "KEYWORD" : "ExposureControlSensingRegionUpperHorizontalEdge"
    },
    "00189439" : {
        "VR" : "SS",
        "VM" : "1",
        "KEYWORD" : "ExposureControlSensingRegionLowerHorizontalEdge"
    },
    "00189440" : {
        "VR" : "SS",
        "VM" : "2",
        "KEYWORD" : "CenterOfCircularExposureControlSensingRegion"
    },
    "00189441" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "RadiusOfCircularExposureControlSensingRegion"
    },
    "00189442" : {
        "VR" : "SS",
        "VM" : "2-n",
        "KEYWORD" : "VerticesOfThePolygonalExposureControlSensingRegion"
    },
    "00189447" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "ColumnAngulationPatient"
    },
    "00189449" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "BeamAngle"
    },
    "00189451" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FrameDetectorParametersSequence"
    },
    "00189452" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "CalculatedAnatomyThickness"
    },
    "00189455" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CalibrationSequence"
    },
    "00189456" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ObjectThicknessSequence"
    },
    "00189457" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PlaneIdentification"
    },
    "00189461" : {
        "VR" : "FL",
        "VM" : "1-2",
        "KEYWORD" : "FieldOfViewDimensionsInFloat"
    },
    "00189462" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IsocenterReferenceSystemSequence"
    },
    "00189463" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "PositionerIsocenterPrimaryAngle"
    },
    "00189464" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "PositionerIsocenterSecondaryAngle"
    },
    "00189465" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "PositionerIsocenterDetectorRotationAngle"
    },
    "00189466" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "TableXPositionToIsocenter"
    },
    "00189467" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "TableYPositionToIsocenter"
    },
    "00189468" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "TableZPositionToIsocenter"
    },
    "00189469" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "TableHorizontalRotationAngle"
    },
    "00189470" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "TableHeadTiltAngle"
    },
    "00189471" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "TableCradleTiltAngle"
    },
    "00189472" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FrameDisplayShutterSequence"
    },
    "00189473" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "AcquiredImageAreaDoseProduct"
    },
    "00189474" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CArmPositionerTabletopRelationship"
    },
    "00189476" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "XRayGeometrySequence"
    },
    "00189477" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IrradiationEventIdentificationSequence"
    },
    "00189504" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "XRay3DFrameTypeSequence"
    },
    "00189506" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ContributingSourcesSequence"
    },
    "00189507" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "XRay3DAcquisitionSequence"
    },
    "00189508" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "PrimaryPositionerScanArc"
    },
    "00189509" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "SecondaryPositionerScanArc"
    },
    "00189510" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "PrimaryPositionerScanStartAngle"
    },
    "00189511" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "SecondaryPositionerScanStartAngle"
    },
    "00189514" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "PrimaryPositionerIncrement"
    },
    "00189515" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "SecondaryPositionerIncrement"
    },
    "00189516" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "StartAcquisitionDateTime"
    },
    "00189517" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "EndAcquisitionDateTime"
    },
    "00189524" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ApplicationName"
    },
    "00189525" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ApplicationVersion"
    },
    "00189526" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ApplicationManufacturer"
    },
    "00189527" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AlgorithmType"
    },
    "00189528" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "AlgorithmDescription"
    },
    "00189530" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "XRay3DReconstructionSequence"
    },
    "00189531" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ReconstructionDescription"
    },
    "00189538" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PerProjectionAcquisitionSequence"
    },
    "00189601" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DiffusionBMatrixSequence"
    },
    "00189602" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "DiffusionBValueXX"
    },
    "00189603" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "DiffusionBValueXY"
    },
    "00189604" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "DiffusionBValueXZ"
    },
    "00189605" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "DiffusionBValueYY"
    },
    "00189606" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "DiffusionBValueYZ"
    },
    "00189607" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "DiffusionBValueZZ"
    },
    "00189701" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "DecayCorrectionDateTime"
    },
    "00189715" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "StartDensityThreshold"
    },
    "00189716" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "StartRelativeDensityDifferenceThreshold"
    },
    "00189717" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "StartCardiacTriggerCountThreshold"
    },
    "00189718" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "StartRespiratoryTriggerCountThreshold"
    },
    "00189719" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TerminationCountsThreshold"
    },
    "00189720" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TerminationDensityThreshold"
    },
    "00189721" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TerminationRelativeDensityThreshold"
    },
    "00189722" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TerminationTimeThreshold"
    },
    "00189723" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TerminationCardiacTriggerCountThreshold"
    },
    "00189724" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TerminationRespiratoryTriggerCountThreshold"
    },
    "00189725" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DetectorGeometry"
    },
    "00189726" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TransverseDetectorSeparation"
    },
    "00189727" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "AxialDetectorDimension"
    },
    "00189729" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "RadiopharmaceuticalAgentNumber"
    },
    "00189732" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PETFrameAcquisitionSequence"
    },
    "00189733" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PETDetectorMotionDetailsSequence"
    },
    "00189734" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PETTableDynamicsSequence"
    },
    "00189735" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PETPositionSequence"
    },
    "00189736" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PETFrameCorrectionFactorsSequence"
    },
    "00189737" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RadiopharmaceuticalUsageSequence"
    },
    "00189738" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AttenuationCorrectionSource"
    },
    "00189739" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfIterations"
    },
    "00189740" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfSubsets"
    },
    "00189749" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PETReconstructionSequence"
    },
    "00189751" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PETFrameTypeSequence"
    },
    "00189755" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TimeOfFlightInformationUsed"
    },
    "00189756" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ReconstructionType"
    },
    "00189758" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DecayCorrected"
    },
    "00189759" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AttenuationCorrected"
    },
    "00189760" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ScatterCorrected"
    },
    "00189761" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DeadTimeCorrected"
    },
    "00189762" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "GantryMotionCorrected"
    },
    "00189763" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PatientMotionCorrected"
    },
    "00189764" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CountLossNormalizationCorrected"
    },
    "00189765" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RandomsCorrected"
    },
    "00189766" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "NonUniformRadialSamplingCorrected"
    },
    "00189767" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SensitivityCalibrated"
    },
    "00189768" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DetectorNormalizationCorrection"
    },
    "00189769" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "IterativeReconstructionMethod"
    },
    "00189770" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AttenuationCorrectionTemporalRelationship"
    },
    "00189771" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PatientPhysiologicalStateSequence"
    },
    "00189772" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PatientPhysiologicalStateCodeSequence"
    },
    "00189801" : {
        "VR" : "FD",
        "VM" : "1-n",
        "KEYWORD" : "DepthsOfFocus"
    },
    "00189803" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ExcludedIntervalsSequence"
    },
    "00189804" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "ExclusionStartDatetime"
    },
    "00189805" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ExclusionDuration"
    },
    "00189806" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "USImageDescriptionSequence"
    },
    "00189807" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ImageDataTypeSequence"
    },
    "00189808" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DataType"
    },
    "00189809" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TransducerScanPatternCodeSequence"
    },
    "0018980B" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AliasedDataType"
    },
    "0018980C" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PositionMeasuringDeviceUsed"
    },
    "0018980D" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TransducerGeometryCodeSequence"
    },
    "0018980E" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TransducerBeamSteeringCodeSequence"
    },
    "0018980F" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TransducerApplicationCodeSequence"
    },
    "0018A001" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ContributingEquipmentSequence"
    },
    "0018A002" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "ContributionDateTime"
    },
    "0018A003" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "ContributionDescription"
    },
    "0020000D" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "StudyInstanceUID"
    },
    "0020000E" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "SeriesInstanceUID"
    },
    "00200010" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "StudyID"
    },
    "00200011" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "SeriesNumber"
    },
    "00200012" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "AcquisitionNumber"
    },
    "00200013" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "InstanceNumber"
    },
    "00200014" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "IsotopeNumber"
    },
    "00200015" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "PhaseNumber"
    },
    "00200016" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "IntervalNumber"
    },
    "00200017" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "TimeSlotNumber"
    },
    "00200018" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "AngleNumber"
    },
    "00200019" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ItemNumber"
    },
    "00200020" : {
        "VR" : "CS",
        "VM" : "2",
        "KEYWORD" : "PatientOrientation"
    },
    "00200022" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "OverlayNumber"
    },
    "00200024" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "CurveNumber"
    },
    "00200026" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "LUTNumber"
    },
    "00200030" : {
        "VR" : "DS",
        "VM" : "3",
        "KEYWORD" : "ImagePosition"
    },
    "00200032" : {
        "VR" : "DS",
        "VM" : "3",
        "KEYWORD" : "ImagePositionPatient"
    },
    "00200035" : {
        "VR" : "DS",
        "VM" : "6",
        "KEYWORD" : "ImageOrientation"
    },
    "00200037" : {
        "VR" : "DS",
        "VM" : "6",
        "KEYWORD" : "ImageOrientationPatient"
    },
    "00200050" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "Location"
    },
    "00200052" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "FrameOfReferenceUID"
    },
    "00200060" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "Laterality"
    },
    "00200062" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ImageLaterality"
    },
    "00200070" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ImageGeometryType"
    },
    "00200080" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "MaskingImage"
    },
    "002000AA" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReportNumber"
    },
    "00200100" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "TemporalPositionIdentifier"
    },
    "00200105" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfTemporalPositions"
    },
    "00200110" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TemporalResolution"
    },
    "00200200" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "SynchronizationFrameOfReferenceUID"
    },
    "00200242" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "SOPInstanceUIDOfConcatenationSource"
    },
    "00201000" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "SeriesInStudy"
    },
    "00201001" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "AcquisitionsInSeries"
    },
    "00201002" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ImagesInAcquisition"
    },
    "00201003" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ImagesInSeries"
    },
    "00201004" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "AcquisitionsInStudy"
    },
    "00201005" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ImagesInStudy"
    },
    "00201020" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "Reference"
    },
    "00201040" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PositionReferenceIndicator"
    },
    "00201041" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SliceLocation"
    },
    "00201070" : {
        "VR" : "IS",
        "VM" : "1-n",
        "KEYWORD" : "OtherStudyNumbers"
    },
    "00201200" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfPatientRelatedStudies"
    },
    "00201202" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfPatientRelatedSeries"
    },
    "00201204" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfPatientRelatedInstances"
    },
    "00201206" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfStudyRelatedSeries"
    },
    "00201208" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfStudyRelatedInstances"
    },
    "00201209" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfSeriesRelatedInstances"
    },
    "002031xx" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "SourceImageIDs"
    },
    "00203401" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ModifyingDeviceID"
    },
    "00203402" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ModifiedImageID"
    },
    "00203403" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "ModifiedImageDate"
    },
    "00203404" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ModifyingDeviceManufacturer"
    },
    "00203405" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "ModifiedImageTime"
    },
    "00203406" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ModifiedImageDescription"
    },
    "00204000" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "ImageComments"
    },
    "00205000" : {
        "VR" : "AT",
        "VM" : "1-n",
        "KEYWORD" : "OriginalImageIdentification"
    },
    "00205002" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "OriginalImageIdentificationNomenclature"
    },
    "00209056" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "StackID"
    },
    "00209057" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "InStackPositionNumber"
    },
    "00209071" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FrameAnatomySequence"
    },
    "00209072" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FrameLaterality"
    },
    "00209111" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FrameContentSequence"
    },
    "00209113" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PlanePositionSequence"
    },
    "00209116" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PlaneOrientationSequence"
    },
    "00209128" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "TemporalPositionIndex"
    },
    "00209153" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "NominalCardiacTriggerDelayTime"
    },
    "00209154" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "NominalCardiacTriggerTimePriorToRPeak"
    },
    "00209155" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "ActualCardiacTriggerTimePriorToRPeak"
    },
    "00209156" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "FrameAcquisitionNumber"
    },
    "00209157" : {
        "VR" : "UL",
        "VM" : "1-n",
        "KEYWORD" : "DimensionIndexValues"
    },
    "00209158" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "FrameComments"
    },
    "00209161" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "ConcatenationUID"
    },
    "00209162" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "InConcatenationNumber"
    },
    "00209163" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "InConcatenationTotalNumber"
    },
    "00209164" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "DimensionOrganizationUID"
    },
    "00209165" : {
        "VR" : "AT",
        "VM" : "1",
        "KEYWORD" : "DimensionIndexPointer"
    },
    "00209167" : {
        "VR" : "AT",
        "VM" : "1",
        "KEYWORD" : "FunctionalGroupPointer"
    },
    "00209213" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "DimensionIndexPrivateCreator"
    },
    "00209221" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DimensionOrganizationSequence"
    },
    "00209222" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DimensionIndexSequence"
    },
    "00209228" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "ConcatenationFrameOffsetNumber"
    },
    "00209238" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "FunctionalGroupPrivateCreator"
    },
    "00209241" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "NominalPercentageOfCardiacPhase"
    },
    "00209245" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "NominalPercentageOfRespiratoryPhase"
    },
    "00209246" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "StartingRespiratoryAmplitude"
    },
    "00209247" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "StartingRespiratoryPhase"
    },
    "00209248" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "EndingRespiratoryAmplitude"
    },
    "00209249" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "EndingRespiratoryPhase"
    },
    "00209250" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RespiratoryTriggerType"
    },
    "00209251" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "RRIntervalTimeNominal"
    },
    "00209252" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ActualCardiacTriggerDelayTime"
    },
    "00209253" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RespiratorySynchronizationSequence"
    },
    "00209254" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "RespiratoryIntervalTime"
    },
    "00209255" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "NominalRespiratoryTriggerDelayTime"
    },
    "00209256" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "RespiratoryTriggerDelayThreshold"
    },
    "00209257" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ActualRespiratoryTriggerDelayTime"
    },
    "00209301" : {
        "VR" : "FD",
        "VM" : "3",
        "KEYWORD" : "ImagePositionVolume"
    },
    "00209302" : {
        "VR" : "FD",
        "VM" : "6",
        "KEYWORD" : "ImageOrientationVolume"
    },
    "00209307" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "UltrasoundAcquisitionGeometry"
    },
    "00209308" : {
        "VR" : "FD",
        "VM" : "3",
        "KEYWORD" : "ApexPosition"
    },
    "00209309" : {
        "VR" : "FD",
        "VM" : "16",
        "KEYWORD" : "VolumeToTransducerMappingMatrix"
    },
    "0020930A" : {
        "VR" : "FD",
        "VM" : "16",
        "KEYWORD" : "VolumeToTableMappingMatrix"
    },
    "0020930C" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PatientFrameOfReferenceSource"
    },
    "0020930D" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TemporalPositionTimeOffset"
    },
    "0020930E" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PlanePositionVolumeSequence"
    },
    "0020930F" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PlaneOrientationVolumeSequence"
    },
    "00209310" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TemporalPositionSequence"
    },
    "00209311" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DimensionOrganizationType"
    },
    "00209312" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "VolumeFrameOfReferenceUID"
    },
    "00209313" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "TableFrameOfReferenceUID"
    },
    "00209421" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "DimensionDescriptionLabel"
    },
    "00209450" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PatientOrientationInFrameSequence"
    },
    "00209453" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "FrameLabel"
    },
    "00209518" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "AcquisitionIndex"
    },
    "00209529" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ContributingSOPInstancesReferenceSequence"
    },
    "00209536" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ReconstructionIndex"
    },
    "00220001" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "LightPathFilterPassThroughWavelength"
    },
    "00220002" : {
        "VR" : "US",
        "VM" : "2",
        "KEYWORD" : "LightPathFilterPassBand"
    },
    "00220003" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ImagePathFilterPassThroughWavelength"
    },
    "00220004" : {
        "VR" : "US",
        "VM" : "2",
        "KEYWORD" : "ImagePathFilterPassBand"
    },
    "00220005" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PatientEyeMovementCommanded"
    },
    "00220006" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PatientEyeMovementCommandCodeSequence"
    },
    "00220007" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "SphericalLensPower"
    },
    "00220008" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "CylinderLensPower"
    },
    "00220009" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "CylinderAxis"
    },
    "0022000A" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "EmmetropicMagnification"
    },
    "0022000B" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "IntraOcularPressure"
    },
    "0022000C" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "HorizontalFieldOfView"
    },
    "0022000D" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PupilDilated"
    },
    "0022000E" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "DegreeOfDilation"
    },
    "00220010" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "StereoBaselineAngle"
    },
    "00220011" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "StereoBaselineDisplacement"
    },
    "00220012" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "StereoHorizontalPixelOffset"
    },
    "00220013" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "StereoVerticalPixelOffset"
    },
    "00220014" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "StereoRotation"
    },
    "00220015" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AcquisitionDeviceTypeCodeSequence"
    },
    "00220016" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IlluminationTypeCodeSequence"
    },
    "00220017" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "LightPathFilterTypeStackCodeSequence"
    },
    "00220018" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ImagePathFilterTypeStackCodeSequence"
    },
    "00220019" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "LensesCodeSequence"
    },
    "0022001A" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ChannelDescriptionCodeSequence"
    },
    "0022001B" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RefractiveStateSequence"
    },
    "0022001C" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MydriaticAgentCodeSequence"
    },
    "0022001D" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RelativeImagePositionCodeSequence"
    },
    "0022001E" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "CameraAngleOfView"
    },
    "00220020" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "StereoPairsSequence"
    },
    "00220021" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "LeftImageSequence"
    },
    "00220022" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RightImageSequence"
    },
    "00220030" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "AxialLengthOfTheEye"
    },
    "00220031" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OphthalmicFrameLocationSequence"
    },
    "00220032" : {
        "VR" : "FL",
        "VM" : "2-2n",
        "KEYWORD" : "ReferenceCoordinates"
    },
    "00220035" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "DepthSpatialResolution"
    },
    "00220036" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "MaximumDepthDistortion"
    },
    "00220037" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "AlongScanSpatialResolution"
    },
    "00220038" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "MaximumAlongScanDistortion"
    },
    "00220039" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OphthalmicImageOrientation"
    },
    "00220041" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "DepthOfTransverseImage"
    },
    "00220042" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MydriaticAgentConcentrationUnitsSequence"
    },
    "00220048" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "AcrossScanSpatialResolution"
    },
    "00220049" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "MaximumAcrossScanDistortion"
    },
    "0022004E" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "MydriaticAgentConcentration"
    },
    "00220055" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "IlluminationWaveLength"
    },
    "00220056" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "IlluminationPower"
    },
    "00220057" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "IlluminationBandwidth"
    },
    "00220058" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MydriaticAgentSequence"
    },
    "00221007" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OphthalmicAxialMeasurementsRightEyeSequence"
    },
    "00221008" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OphthalmicAxialMeasurementsLeftEyeSequence"
    },
    "00221010" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OphthalmicAxialLengthMeasurementsType"
    },
    "00221019" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "OphthalmicAxialLength"
    },
    "00221024" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "LensStatusCodeSequence"
    },
    "00221025" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "VitreousStatusCodeSequence"
    },
    "00221028" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IOLFormulaCodeSequence"
    },
    "00221029" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "IOLFormulaDetail"
    },
    "00221033" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "KeratometerIndex"
    },
    "00221035" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SourceOfOphthalmicAxialLengthCodeSequence"
    },
    "00221037" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "TargetRefraction"
    },
    "00221039" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RefractiveProcedureOccurred"
    },
    "00221040" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RefractiveSurgeryTypeCodeSequence"
    },
    "00221044" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OphthalmicUltrasoundAxialMeasurementsTypeCodeSequence"
    },
    "00221050" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OphthalmicAxialLengthMeasurementsSequence"
    },
    "00221053" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "IOLPower"
    },
    "00221054" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "PredictedRefractiveError"
    },
    "00221059" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "OphthalmicAxialLengthVelocity"
    },
    "00221065" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "LensStatusDescription"
    },
    "00221066" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "VitreousStatusDescription"
    },
    "00221090" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IOLPowerSequence"
    },
    "00221092" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "LensConstantSequence"
    },
    "00221093" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "IOLManufacturer"
    },
    "00221094" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "LensConstantDescription"
    },
    "00221096" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "KeratometryMeasurementTypeCodeSequence"
    },
    "00221100" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedOphthalmicAxialMeasurementsSequence"
    },
    "00221101" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OphthalmicAxialLengthMeasurementsSegmentNameCodeSequence"
    },
    "00221103" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RefractiveErrorBeforeRefractiveSurgeryCodeSequence"
    },
    "00221121" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "IOLPowerForExactEmmetropia"
    },
    "00221122" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "IOLPowerForExactTargetRefraction"
    },
    "00221125" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AnteriorChamberDepthDefinitionCodeSequence"
    },
    "00221130" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "LensThickness"
    },
    "00221131" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "AnteriorChamberDepth"
    },
    "00221132" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SourceOfLensThicknessDataCodeSequence"
    },
    "00221133" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SourceOfAnteriorChamberDepthDataCodeSequence"
    },
    "00221135" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SourceOfRefractiveErrorDataCodeSequence"
    },
    "00221140" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OphthalmicAxialLengthMeasurementModified"
    },
    "00221150" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OphthalmicAxialLengthDataSourceCodeSequence"
    },
    "00221153" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OphthalmicAxialLengthAcquisitionMethodCodeSequence"
    },
    "00221155" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "SignalToNoiseRatio"
    },
    "00221159" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "OphthalmicAxialLengthDataSourceDescription"
    },
    "00221210" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OphthalmicAxialLengthMeasurementsTotalLengthSequence"
    },
    "00221211" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OphthalmicAxialLengthMeasurementsSegmentalLengthSequence"
    },
    "00221212" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OphthalmicAxialLengthMeasurementsLengthSummationSequence"
    },
    "00221220" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "UltrasoundOphthalmicAxialLengthMeasurementsSequence"
    },
    "00221225" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OpticalOphthalmicAxialLengthMeasurementsSequence"
    },
    "00221230" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "UltrasoundSelectedOphthalmicAxialLengthSequence"
    },
    "00221250" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OphthalmicAxialLengthSelectionMethodCodeSequence"
    },
    "00221255" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OpticalSelectedOphthalmicAxialLengthSequence"
    },
    "00221257" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SelectedSegmentalOphthalmicAxialLengthSequence"
    },
    "00221260" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SelectedTotalOphthalmicAxialLengthSequence"
    },
    "00221262" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OphthalmicAxialLengthQualityMetricSequence"
    },
    "00221273" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "OphthalmicAxialLengthQualityMetricTypeDescription"
    },
    "00221300" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IntraocularLensCalculationsRightEyeSequence"
    },
    "00221310" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IntraocularLensCalculationsLeftEyeSequence"
    },
    "00221330" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedOphthalmicAxialLengthMeasurementQCImageSequence"
    },
    "00240010" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "VisualFieldHorizontalExtent"
    },
    "00240011" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "VisualFieldVerticalExtent"
    },
    "00240012" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "VisualFieldShape"
    },
    "00240016" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ScreeningTestModeCodeSequence"
    },
    "00240018" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "MaximumStimulusLuminance"
    },
    "00240020" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "BackgroundLuminance"
    },
    "00240021" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "StimulusColorCodeSequence"
    },
    "00240024" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BackgroundIlluminationColorCodeSequence"
    },
    "00240025" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "StimulusArea"
    },
    "00240028" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "StimulusPresentationTime"
    },
    "00240032" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FixationSequence"
    },
    "00240033" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FixationMonitoringCodeSequence"
    },
    "00240034" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "VisualFieldCatchTrialSequence"
    },
    "00240035" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "FixationCheckedQuantity"
    },
    "00240036" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "PatientNotProperlyFixatedQuantity"
    },
    "00240037" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PresentedVisualStimuliDataFlag"
    },
    "00240038" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfVisualStimuli"
    },
    "00240039" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ExcessiveFixationLossesDataFlag"
    },
    "00240040" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ExcessiveFixationLosses"
    },
    "00240042" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "StimuliRetestingQuantity"
    },
    "00240044" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "CommentsOnPatientPerformanceOfVisualField"
    },
    "00240045" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FalseNegativesEstimateFlag"
    },
    "00240046" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "FalseNegativesEstimate"
    },
    "00240048" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NegativeCatchTrialsQuantity"
    },
    "00240050" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "FalseNegativesQuantity"
    },
    "00240051" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ExcessiveFalseNegativesDataFlag"
    },
    "00240052" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ExcessiveFalseNegatives"
    },
    "00240053" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FalsePositivesEstimateFlag"
    },
    "00240054" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "FalsePositivesEstimate"
    },
    "00240055" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CatchTrialsDataFlag"
    },
    "00240056" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "PositiveCatchTrialsQuantity"
    },
    "00240057" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TestPointNormalsDataFlag"
    },
    "00240058" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TestPointNormalsSequence"
    },
    "00240059" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "GlobalDeviationProbabilityNormalsFlag"
    },
    "00240060" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "FalsePositivesQuantity"
    },
    "00240061" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ExcessiveFalsePositivesDataFlag"
    },
    "00240062" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ExcessiveFalsePositives"
    },
    "00240063" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "VisualFieldTestNormalsFlag"
    },
    "00240064" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ResultsNormalsSequence"
    },
    "00240065" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AgeCorrectedSensitivityDeviationAlgorithmSequence"
    },
    "00240066" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "GlobalDeviationFromNormal"
    },
    "00240067" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "GeneralizedDefectSensitivityDeviationAlgorithmSequence"
    },
    "00240068" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "LocalizedDeviationfromNormal"
    },
    "00240069" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PatientReliabilityIndicator"
    },
    "00240070" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "VisualFieldMeanSensitivity"
    },
    "00240071" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "GlobalDeviationProbability"
    },
    "00240072" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "LocalDeviationProbabilityNormalsFlag"
    },
    "00240073" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "LocalizedDeviationProbability"
    },
    "00240074" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ShortTermFluctuationCalculated"
    },
    "00240075" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "ShortTermFluctuation"
    },
    "00240076" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ShortTermFluctuationProbabilityCalculated"
    },
    "00240077" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "ShortTermFluctuationProbability"
    },
    "00240078" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CorrectedLocalizedDeviationFromNormalCalculated"
    },
    "00240079" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "CorrectedLocalizedDeviationFromNormal"
    },
    "00240080" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CorrectedLocalizedDeviationFromNormalProbabilityCalculated"
    },
    "00240081" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "CorrectedLocalizedDeviationFromNormalProbability"
    },
    "00240083" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "GlobalDeviationProbabilitySequence"
    },
    "00240085" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "LocalizedDeviationProbabilitySequence"
    },
    "00240086" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FovealSensitivityMeasured"
    },
    "00240087" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "FovealSensitivity"
    },
    "00240088" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "VisualFieldTestDuration"
    },
    "00240089" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "VisualFieldTestPointSequence"
    },
    "00240090" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "VisualFieldTestPointXCoordinate"
    },
    "00240091" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "VisualFieldTestPointYCoordinate"
    },
    "00240092" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "AgeCorrectedSensitivityDeviationValue"
    },
    "00240093" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "StimulusResults"
    },
    "00240094" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "SensitivityValue"
    },
    "00240095" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RetestStimulusSeen"
    },
    "00240096" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "RetestSensitivityValue"
    },
    "00240097" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "VisualFieldTestPointNormalsSequence"
    },
    "00240098" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "QuantifiedDefect"
    },
    "00240100" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "AgeCorrectedSensitivityDeviationProbabilityValue"
    },
    "00240102" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "GeneralizedDefectCorrectedSensitivityDeviationFlag "
    },
    "00240103" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "GeneralizedDefectCorrectedSensitivityDeviationValue "
    },
    "00240104" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "GeneralizedDefectCorrectedSensitivityDeviationProbabilityValue"
    },
    "00240105" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "MinimumSensitivityValue"
    },
    "00240106" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BlindSpotLocalized"
    },
    "00240107" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "BlindSpotXCoordinate"
    },
    "00240108" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "BlindSpotYCoordinate "
    },
    "00240110" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "VisualAcuityMeasurementSequence"
    },
    "00240112" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RefractiveParametersUsedOnPatientSequence"
    },
    "00240113" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "MeasurementLaterality"
    },
    "00240114" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OphthalmicPatientClinicalInformationLeftEyeSequence"
    },
    "00240115" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OphthalmicPatientClinicalInformationRightEyeSequence"
    },
    "00240117" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FovealPointNormativeDataFlag"
    },
    "00240118" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "FovealPointProbabilityValue"
    },
    "00240120" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ScreeningBaselineMeasured"
    },
    "00240122" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ScreeningBaselineMeasuredSequence"
    },
    "00240124" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ScreeningBaselineType"
    },
    "00240126" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "ScreeningBaselineValue"
    },
    "00240202" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "AlgorithmSource"
    },
    "00240306" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "DataSetName"
    },
    "00240307" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "DataSetVersion"
    },
    "00240308" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "DataSetSource"
    },
    "00240309" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "DataSetDescription"
    },
    "00240317" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "VisualFieldTestReliabilityGlobalIndexSequence"
    },
    "00240320" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "VisualFieldGlobalResultsIndexSequence"
    },
    "00240325" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DataObservationSequence"
    },
    "00240338" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "IndexNormalsFlag"
    },
    "00240341" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "IndexProbability"
    },
    "00240344" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IndexProbabilitySequence"
    },
    "00280002" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "SamplesPerPixel"
    },
    "00280003" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "SamplesPerPixelUsed"
    },
    "00280004" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PhotometricInterpretation"
    },
    "00280005" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ImageDimensions"
    },
    "00280006" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "PlanarConfiguration"
    },
    "00280008" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfFrames"
    },
    "00280009" : {
        "VR" : "AT",
        "VM" : "1-n",
        "KEYWORD" : "FrameIncrementPointer"
    },
    "0028000A" : {
        "VR" : "AT",
        "VM" : "1-n",
        "KEYWORD" : "FrameDimensionPointer"
    },
    "00280010" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "Rows"
    },
    "00280011" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "Columns"
    },
    "00280012" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "Planes"
    },
    "00280014" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "UltrasoundColorDataPresent"
    },
    "00280030" : {
        "VR" : "DS",
        "VM" : "2",
        "KEYWORD" : "PixelSpacing"
    },
    "00280031" : {
        "VR" : "DS",
        "VM" : "2",
        "KEYWORD" : "ZoomFactor"
    },
    "00280032" : {
        "VR" : "DS",
        "VM" : "2",
        "KEYWORD" : "ZoomCenter"
    },
    "00280034" : {
        "VR" : "IS",
        "VM" : "2",
        "KEYWORD" : "PixelAspectRatio"
    },
    "00280040" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ImageFormat"
    },
    "00280050" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "ManipulatedImage"
    },
    "00280051" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "CorrectedImage"
    },
    "0028005F" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "CompressionRecognitionCode"
    },
    "00280060" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CompressionCode"
    },
    "00280061" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "CompressionOriginator"
    },
    "00280062" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "CompressionLabel"
    },
    "00280063" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "CompressionDescription"
    },
    "00280065" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "CompressionSequence"
    },
    "00280066" : {
        "VR" : "AT",
        "VM" : "1-n",
        "KEYWORD" : "CompressionStepPointers"
    },
    "00280068" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "RepeatInterval"
    },
    "00280069" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "BitsGrouped"
    },
    "00280070" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "PerimeterTable"
    },
    "00280071" : {
        "VR" : "US|SS",
        "VM" : "1",
        "KEYWORD" : "PerimeterValue"
    },
    "00280080" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "PredictorRows"
    },
    "00280081" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "PredictorColumns"
    },
    "00280082" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "PredictorConstants"
    },
    "00280090" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BlockedPixels"
    },
    "00280091" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "BlockRows"
    },
    "00280092" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "BlockColumns"
    },
    "00280093" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "RowOverlap"
    },
    "00280094" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ColumnOverlap"
    },
    "00280100" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "BitsAllocated"
    },
    "00280101" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "BitsStored"
    },
    "00280102" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "HighBit"
    },
    "00280103" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "PixelRepresentation"
    },
    "00280104" : {
        "VR" : "US|SS",
        "VM" : "1",
        "KEYWORD" : "SmallestValidPixelValue"
    },
    "00280105" : {
        "VR" : "US|SS",
        "VM" : "1",
        "KEYWORD" : "LargestValidPixelValue"
    },
    "00280106" : {
        "VR" : "US|SS",
        "VM" : "1",
        "KEYWORD" : "SmallestImagePixelValue"
    },
    "00280107" : {
        "VR" : "US|SS",
        "VM" : "1",
        "KEYWORD" : "LargestImagePixelValue"
    },
    "00280108" : {
        "VR" : "US|SS",
        "VM" : "1",
        "KEYWORD" : "SmallestPixelValueInSeries"
    },
    "00280109" : {
        "VR" : "US|SS",
        "VM" : "1",
        "KEYWORD" : "LargestPixelValueInSeries"
    },
    "00280110" : {
        "VR" : "US|SS",
        "VM" : "1",
        "KEYWORD" : "SmallestImagePixelValueInPlane"
    },
    "00280111" : {
        "VR" : "US|SS",
        "VM" : "1",
        "KEYWORD" : "LargestImagePixelValueInPlane"
    },
    "00280120" : {
        "VR" : "US|SS",
        "VM" : "1",
        "KEYWORD" : "PixelPaddingValue"
    },
    "00280121" : {
        "VR" : "US|SS",
        "VM" : "1",
        "KEYWORD" : "PixelPaddingRangeLimit"
    },
    "00280200" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ImageLocation"
    },
    "00280300" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "QualityControlImage"
    },
    "00280301" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BurnedInAnnotation"
    },
    "00280302" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RecognizableVisualFeatures"
    },
    "00280303" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "LongitudinalTemporalInformationModified"
    },
    "00280400" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "TransformLabel"
    },
    "00280401" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "TransformVersionNumber"
    },
    "00280402" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfTransformSteps"
    },
    "00280403" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "SequenceOfCompressedData"
    },
    "00280404" : {
        "VR" : "AT",
        "VM" : "1-n",
        "KEYWORD" : "DetailsOfCoefficients"
    },
    "002804x0" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "RowsForNthOrderCoefficients"
    },
    "002804x1" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ColumnsForNthOrderCoefficients"
    },
    "002804x2" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "CoefficientCoding"
    },
    "002804x3" : {
        "VR" : "AT",
        "VM" : "1-n",
        "KEYWORD" : "CoefficientCodingPointers"
    },
    "00280700" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "DCTLabel"
    },
    "00280701" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "DataBlockDescription"
    },
    "00280702" : {
        "VR" : "AT",
        "VM" : "1-n",
        "KEYWORD" : "DataBlock"
    },
    "00280710" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NormalizationFactorFormat"
    },
    "00280720" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ZonalMapNumberFormat"
    },
    "00280721" : {
        "VR" : "AT",
        "VM" : "1-n",
        "KEYWORD" : "ZonalMapLocation"
    },
    "00280722" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ZonalMapFormat"
    },
    "00280730" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "AdaptiveMapFormat"
    },
    "00280740" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "CodeNumberFormat"
    },
    "002808x0" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "CodeLabel"
    },
    "002808x2" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfTables"
    },
    "002808x3" : {
        "VR" : "AT",
        "VM" : "1-n",
        "KEYWORD" : "CodeTableLocation"
    },
    "002808x4" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "BitsForCodeWord"
    },
    "002808x8" : {
        "VR" : "AT",
        "VM" : "1-n",
        "KEYWORD" : "ImageDataLocation"
    },
    "00280A02" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PixelSpacingCalibrationType"
    },
    "00280A04" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PixelSpacingCalibrationDescription"
    },
    "00281040" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PixelIntensityRelationship"
    },
    "00281041" : {
        "VR" : "SS",
        "VM" : "1",
        "KEYWORD" : "PixelIntensityRelationshipSign"
    },
    "00281050" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "WindowCenter"
    },
    "00281051" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "WindowWidth"
    },
    "00281052" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "RescaleIntercept"
    },
    "00281053" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "RescaleSlope"
    },
    "00281054" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "RescaleType"
    },
    "00281055" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "WindowCenterWidthExplanation"
    },
    "00281056" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "VOILUTFunction"
    },
    "00281080" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "GrayScale"
    },
    "00281090" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RecommendedViewingMode"
    },
    "00281100" : {
        "VR" : "US|SS",
        "VM" : "3",
        "KEYWORD" : "GrayLookupTableDescriptor"
    },
    "00281101" : {
        "VR" : "US|SS",
        "VM" : "3",
        "KEYWORD" : "RedPaletteColorLookupTableDescriptor"
    },
    "00281102" : {
        "VR" : "US|SS",
        "VM" : "3",
        "KEYWORD" : "GreenPaletteColorLookupTableDescriptor"
    },
    "00281103" : {
        "VR" : "US|SS",
        "VM" : "3",
        "KEYWORD" : "BluePaletteColorLookupTableDescriptor"
    },
    "00281104" : {
        "VR" : "US",
        "VM" : "3",
        "KEYWORD" : "AlphaPaletteColorLookupTableDescriptor"
    },
    "00281111" : {
        "VR" : "US|SS",
        "VM" : "4",
        "KEYWORD" : "LargeRedPaletteColorLookupTableDescriptor"
    },
    "00281112" : {
        "VR" : "US|SS",
        "VM" : "4",
        "KEYWORD" : "LargeGreenPaletteColorLookupTableDescriptor"
    },
    "00281113" : {
        "VR" : "US|SS",
        "VM" : "4",
        "KEYWORD" : "LargeBluePaletteColorLookupTableDescriptor"
    },
    "00281199" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "PaletteColorLookupTableUID"
    },
    "00281200" : {
        "VR" : "US|SS|OW",
        "VM" : "1-n1",
        "KEYWORD" : "GrayLookupTableData"
    },
    "00281201" : {
        "VR" : "OW",
        "VM" : "1",
        "KEYWORD" : "RedPaletteColorLookupTableData"
    },
    "00281202" : {
        "VR" : "OW",
        "VM" : "1",
        "KEYWORD" : "GreenPaletteColorLookupTableData"
    },
    "00281203" : {
        "VR" : "OW",
        "VM" : "1",
        "KEYWORD" : "BluePaletteColorLookupTableData"
    },
    "00281204" : {
        "VR" : "OW",
        "VM" : "1",
        "KEYWORD" : "AlphaPaletteColorLookupTableData"
    },
    "00281211" : {
        "VR" : "OW",
        "VM" : "1",
        "KEYWORD" : "LargeRedPaletteColorLookupTableData"
    },
    "00281212" : {
        "VR" : "OW",
        "VM" : "1",
        "KEYWORD" : "LargeGreenPaletteColorLookupTableData"
    },
    "00281213" : {
        "VR" : "OW",
        "VM" : "1",
        "KEYWORD" : "LargeBluePaletteColorLookupTableData"
    },
    "00281214" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "LargePaletteColorLookupTableUID"
    },
    "00281221" : {
        "VR" : "OW",
        "VM" : "1",
        "KEYWORD" : "SegmentedRedPaletteColorLookupTableData"
    },
    "00281222" : {
        "VR" : "OW",
        "VM" : "1",
        "KEYWORD" : "SegmentedGreenPaletteColorLookupTableData"
    },
    "00281223" : {
        "VR" : "OW",
        "VM" : "1",
        "KEYWORD" : "SegmentedBluePaletteColorLookupTableData"
    },
    "00281300" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BreastImplantPresent"
    },
    "00281350" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PartialView"
    },
    "00281351" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "PartialViewDescription"
    },
    "00281352" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PartialViewCodeSequence"
    },
    "0028135A" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SpatialLocationsPreserved"
    },
    "00281401" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DataFrameAssignmentSequence"
    },
    "00281402" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DataPathAssignment"
    },
    "00281403" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "BitsMappedToColorLookupTable"
    },
    "00281404" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BlendingLUT1Sequence"
    },
    "00281405" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BlendingLUT1TransferFunction"
    },
    "00281406" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "BlendingWeightConstant"
    },
    "00281407" : {
        "VR" : "US",
        "VM" : "3",
        "KEYWORD" : "BlendingLookupTableDescriptor"
    },
    "00281408" : {
        "VR" : "OW",
        "VM" : "1",
        "KEYWORD" : "BlendingLookupTableData"
    },
    "0028140B" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "EnhancedPaletteColorLookupTableSequence"
    },
    "0028140C" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BlendingLUT2Sequence"
    },
    "0028140D" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BlendingLUT2TransferFunction"
    },
    "0028140E" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DataPathID"
    },
    "0028140F" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RGBLUTTransferFunction"
    },
    "00281410" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AlphaLUTTransferFunction"
    },
    "00282000" : {
        "VR" : "OB",
        "VM" : "1",
        "KEYWORD" : "ICCProfile"
    },
    "00282110" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "LossyImageCompression"
    },
    "00282112" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "LossyImageCompressionRatio"
    },
    "00282114" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "LossyImageCompressionMethod"
    },
    "00283000" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ModalityLUTSequence"
    },
    "00283002" : {
        "VR" : "US|SS",
        "VM" : "3",
        "KEYWORD" : "LUTDescriptor"
    },
    "00283003" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "LUTExplanation"
    },
    "00283004" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ModalityLUTType"
    },
    "00283006" : {
        "VR" : "US|OW",
        "VM" : "1-n1",
        "KEYWORD" : "LUTData"
    },
    "00283010" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "VOILUTSequence"
    },
    "00283110" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SoftcopyVOILUTSequence"
    },
    "00284000" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "ImagePresentationComments"
    },
    "00285000" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BiPlaneAcquisitionSequence"
    },
    "00286010" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "RepresentativeFrameNumber"
    },
    "00286020" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "FrameNumbersOfInterest"
    },
    "00286022" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "FrameOfInterestDescription"
    },
    "00286023" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "FrameOfInterestType"
    },
    "00286030" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "MaskPointers"
    },
    "00286040" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "RWavePointer"
    },
    "00286100" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MaskSubtractionSequence"
    },
    "00286101" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "MaskOperation"
    },
    "00286102" : {
        "VR" : "US",
        "VM" : "2-2n",
        "KEYWORD" : "ApplicableFrameRange"
    },
    "00286110" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "MaskFrameNumbers"
    },
    "00286112" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ContrastFrameAveraging"
    },
    "00286114" : {
        "VR" : "FL",
        "VM" : "2",
        "KEYWORD" : "MaskSubPixelShift"
    },
    "00286120" : {
        "VR" : "SS",
        "VM" : "1",
        "KEYWORD" : "TIDOffset"
    },
    "00286190" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "MaskOperationExplanation"
    },
    "00287FE0" : {
        "VR" : "UT",
        "VM" : "1",
        "KEYWORD" : "PixelDataProviderURL"
    },
    "00289001" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "DataPointRows"
    },
    "00289002" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "DataPointColumns"
    },
    "00289003" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SignalDomainColumns"
    },
    "00289099" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "LargestMonochromePixelValue"
    },
    "00289108" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DataRepresentation"
    },
    "00289110" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PixelMeasuresSequence"
    },
    "00289132" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FrameVOILUTSequence"
    },
    "00289145" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PixelValueTransformationSequence"
    },
    "00289235" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SignalDomainRows"
    },
    "00289411" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "DisplayFilterPercentage"
    },
    "00289415" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FramePixelShiftSequence"
    },
    "00289416" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "SubtractionItemID"
    },
    "00289422" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PixelIntensityRelationshipLUTSequence"
    },
    "00289443" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FramePixelDataPropertiesSequence"
    },
    "00289444" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "GeometricalProperties"
    },
    "00289445" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "GeometricMaximumDistortion"
    },
    "00289446" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "ImageProcessingApplied"
    },
    "00289454" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "MaskSelectionMode"
    },
    "00289474" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "LUTFunction"
    },
    "00289478" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "MaskVisibilityPercentage"
    },
    "00289501" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PixelShiftSequence"
    },
    "00289502" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RegionPixelShiftSequence"
    },
    "00289503" : {
        "VR" : "SS",
        "VM" : "2-2n",
        "KEYWORD" : "VerticesOfTheRegion"
    },
    "00289505" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MultiFramePresentationSequence"
    },
    "00289506" : {
        "VR" : "US",
        "VM" : "2-2n",
        "KEYWORD" : "PixelShiftFrameRange"
    },
    "00289507" : {
        "VR" : "US",
        "VM" : "2-2n",
        "KEYWORD" : "LUTFrameRange"
    },
    "00289520" : {
        "VR" : "DS",
        "VM" : "16",
        "KEYWORD" : "ImageToEquipmentMappingMatrix"
    },
    "00289537" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "EquipmentCoordinateSystemIdentification"
    },
    "0032000A" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "StudyStatusID"
    },
    "0032000C" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "StudyPriorityID"
    },
    "00320012" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "StudyIDIssuer"
    },
    "00320032" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "StudyVerifiedDate"
    },
    "00320033" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "StudyVerifiedTime"
    },
    "00320034" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "StudyReadDate"
    },
    "00320035" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "StudyReadTime"
    },
    "00321000" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "ScheduledStudyStartDate"
    },
    "00321001" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "ScheduledStudyStartTime"
    },
    "00321010" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "ScheduledStudyStopDate"
    },
    "00321011" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "ScheduledStudyStopTime"
    },
    "00321020" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ScheduledStudyLocation"
    },
    "00321021" : {
        "VR" : "AE",
        "VM" : "1-n",
        "KEYWORD" : "ScheduledStudyLocationAETitle"
    },
    "00321030" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ReasonForStudy"
    },
    "00321031" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RequestingPhysicianIdentificationSequence"
    },
    "00321032" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "RequestingPhysician"
    },
    "00321033" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "RequestingService"
    },
    "00321034" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RequestingServiceCodeSequence"
    },
    "00321040" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "StudyArrivalDate"
    },
    "00321041" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "StudyArrivalTime"
    },
    "00321050" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "StudyCompletionDate"
    },
    "00321051" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "StudyCompletionTime"
    },
    "00321055" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "StudyComponentStatusID"
    },
    "00321060" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "RequestedProcedureDescription"
    },
    "00321064" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RequestedProcedureCodeSequence"
    },
    "00321070" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "RequestedContrastAgent"
    },
    "00324000" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "StudyComments"
    },
    "00380004" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedPatientAliasSequence"
    },
    "00380008" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "VisitStatusID"
    },
    "00380010" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "AdmissionID"
    },
    "00380011" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "IssuerOfAdmissionID"
    },
    "00380014" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IssuerOfAdmissionIDSequence"
    },
    "00380016" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "RouteOfAdmissions"
    },
    "0038001A" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "ScheduledAdmissionDate"
    },
    "0038001B" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "ScheduledAdmissionTime"
    },
    "0038001C" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "ScheduledDischargeDate"
    },
    "0038001D" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "ScheduledDischargeTime"
    },
    "0038001E" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ScheduledPatientInstitutionResidence"
    },
    "00380020" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "AdmittingDate"
    },
    "00380021" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "AdmittingTime"
    },
    "00380030" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "DischargeDate"
    },
    "00380032" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "DischargeTime"
    },
    "00380040" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "DischargeDiagnosisDescription"
    },
    "00380044" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DischargeDiagnosisCodeSequence"
    },
    "00380050" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SpecialNeeds"
    },
    "00380060" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ServiceEpisodeID"
    },
    "00380061" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "IssuerOfServiceEpisodeID"
    },
    "00380062" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ServiceEpisodeDescription"
    },
    "00380064" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IssuerOfServiceEpisodeIDSequence"
    },
    "00380100" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PertinentDocumentsSequence"
    },
    "00380300" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "CurrentPatientLocation"
    },
    "00380400" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PatientInstitutionResidence"
    },
    "00380500" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PatientState"
    },
    "00380502" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PatientClinicalTrialParticipationSequence"
    },
    "00384000" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "VisitComments"
    },
    "003A0004" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "WaveformOriginality"
    },
    "003A0005" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfWaveformChannels"
    },
    "003A0010" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "NumberOfWaveformSamples"
    },
    "003A001A" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SamplingFrequency"
    },
    "003A0020" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "MultiplexGroupLabel"
    },
    "003A0200" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ChannelDefinitionSequence"
    },
    "003A0202" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "WaveformChannelNumber"
    },
    "003A0203" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "ChannelLabel"
    },
    "003A0205" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "ChannelStatus"
    },
    "003A0208" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ChannelSourceSequence"
    },
    "003A0209" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ChannelSourceModifiersSequence"
    },
    "003A020A" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SourceWaveformSequence"
    },
    "003A020C" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ChannelDerivationDescription"
    },
    "003A0210" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ChannelSensitivity"
    },
    "003A0211" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ChannelSensitivityUnitsSequence"
    },
    "003A0212" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ChannelSensitivityCorrectionFactor"
    },
    "003A0213" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ChannelBaseline"
    },
    "003A0214" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ChannelTimeSkew"
    },
    "003A0215" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ChannelSampleSkew"
    },
    "003A0218" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ChannelOffset"
    },
    "003A021A" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "WaveformBitsStored"
    },
    "003A0220" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "FilterLowFrequency"
    },
    "003A0221" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "FilterHighFrequency"
    },
    "003A0222" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "NotchFilterFrequency"
    },
    "003A0223" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "NotchFilterBandwidth"
    },
    "003A0230" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "WaveformDataDisplayScale"
    },
    "003A0231" : {
        "VR" : "US",
        "VM" : "3",
        "KEYWORD" : "WaveformDisplayBackgroundCIELabValue"
    },
    "003A0240" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "WaveformPresentationGroupSequence"
    },
    "003A0241" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "PresentationGroupNumber"
    },
    "003A0242" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ChannelDisplaySequence"
    },
    "003A0244" : {
        "VR" : "US",
        "VM" : "3",
        "KEYWORD" : "ChannelRecommendedDisplayCIELabValue"
    },
    "003A0245" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "ChannelPosition"
    },
    "003A0246" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DisplayShadingFlag"
    },
    "003A0247" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "FractionalChannelDisplayScale"
    },
    "003A0248" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "AbsoluteChannelDisplayScale"
    },
    "003A0300" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MultiplexedAudioChannelsDescriptionCodeSequence"
    },
    "003A0301" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ChannelIdentificationCode"
    },
    "003A0302" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ChannelMode"
    },
    "00400001" : {
        "VR" : "AE",
        "VM" : "1-n",
        "KEYWORD" : "ScheduledStationAETitle"
    },
    "00400002" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "ScheduledProcedureStepStartDate"
    },
    "00400003" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "ScheduledProcedureStepStartTime"
    },
    "00400004" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "ScheduledProcedureStepEndDate"
    },
    "00400005" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "ScheduledProcedureStepEndTime"
    },
    "00400006" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "ScheduledPerformingPhysicianName"
    },
    "00400007" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ScheduledProcedureStepDescription"
    },
    "00400008" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ScheduledProtocolCodeSequence"
    },
    "00400009" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "ScheduledProcedureStepID"
    },
    "0040000A" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "StageCodeSequence"
    },
    "0040000B" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ScheduledPerformingPhysicianIdentificationSequence"
    },
    "00400010" : {
        "VR" : "SH",
        "VM" : "1-n",
        "KEYWORD" : "ScheduledStationName"
    },
    "00400011" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "ScheduledProcedureStepLocation"
    },
    "00400012" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PreMedication"
    },
    "00400020" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ScheduledProcedureStepStatus"
    },
    "00400026" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OrderPlacerIdentifierSequence"
    },
    "00400027" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OrderFillerIdentifierSequence"
    },
    "00400031" : {
        "VR" : "UT",
        "VM" : "1",
        "KEYWORD" : "LocalNamespaceEntityID"
    },
    "00400032" : {
        "VR" : "UT",
        "VM" : "1",
        "KEYWORD" : "UniversalEntityID"
    },
    "00400033" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "UniversalEntityIDType"
    },
    "00400035" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "IdentifierTypeCode"
    },
    "00400036" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AssigningFacilitySequence"
    },
    "00400039" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AssigningJurisdictionCodeSequence"
    },
    "0040003A" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AssigningAgencyOrDepartmentCodeSequence"
    },
    "00400100" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ScheduledProcedureStepSequence"
    },
    "00400220" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedNonImageCompositeSOPInstanceSequence"
    },
    "00400241" : {
        "VR" : "AE",
        "VM" : "1",
        "KEYWORD" : "PerformedStationAETitle"
    },
    "00400242" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "PerformedStationName"
    },
    "00400243" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "PerformedLocation"
    },
    "00400244" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "PerformedProcedureStepStartDate"
    },
    "00400245" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "PerformedProcedureStepStartTime"
    },
    "00400250" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "PerformedProcedureStepEndDate"
    },
    "00400251" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "PerformedProcedureStepEndTime"
    },
    "00400252" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PerformedProcedureStepStatus"
    },
    "00400253" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "PerformedProcedureStepID"
    },
    "00400254" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PerformedProcedureStepDescription"
    },
    "00400255" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PerformedProcedureTypeDescription"
    },
    "00400260" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PerformedProtocolCodeSequence"
    },
    "00400261" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PerformedProtocolType"
    },
    "00400270" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ScheduledStepAttributesSequence"
    },
    "00400275" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RequestAttributesSequence"
    },
    "00400280" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "CommentsOnThePerformedProcedureStep"
    },
    "00400281" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PerformedProcedureStepDiscontinuationReasonCodeSequence"
    },
    "00400293" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "QuantitySequence"
    },
    "00400294" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "Quantity"
    },
    "00400295" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MeasuringUnitsSequence"
    },
    "00400296" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BillingItemSequence"
    },
    "00400300" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "TotalTimeOfFluoroscopy"
    },
    "00400301" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "TotalNumberOfExposures"
    },
    "00400302" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "EntranceDose"
    },
    "00400303" : {
        "VR" : "US",
        "VM" : "1-2",
        "KEYWORD" : "ExposedArea"
    },
    "00400306" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DistanceSourceToEntrance"
    },
    "00400307" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DistanceSourceToSupport"
    },
    "0040030E" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ExposureDoseSequence"
    },
    "00400310" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "CommentsOnRadiationDose"
    },
    "00400312" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "XRayOutput"
    },
    "00400314" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "HalfValueLayer"
    },
    "00400316" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "OrganDose"
    },
    "00400318" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OrganExposed"
    },
    "00400320" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BillingProcedureStepSequence"
    },
    "00400321" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FilmConsumptionSequence"
    },
    "00400324" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BillingSuppliesAndDevicesSequence"
    },
    "00400330" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedProcedureStepSequence"
    },
    "00400340" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PerformedSeriesSequence"
    },
    "00400400" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "CommentsOnTheScheduledProcedureStep"
    },
    "00400440" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ProtocolContextSequence"
    },
    "00400441" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ContentItemModifierSequence"
    },
    "00400500" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ScheduledSpecimenSequence"
    },
    "0040050A" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SpecimenAccessionNumber"
    },
    "00400512" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ContainerIdentifier"
    },
    "00400513" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IssuerOfTheContainerIdentifierSequence"
    },
    "00400515" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AlternateContainerIdentifierSequence"
    },
    "00400518" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ContainerTypeCodeSequence"
    },
    "0040051A" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ContainerDescription"
    },
    "00400520" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ContainerComponentSequence"
    },
    "00400550" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SpecimenSequence"
    },
    "00400551" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SpecimenIdentifier"
    },
    "00400552" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SpecimenDescriptionSequenceTrial"
    },
    "00400553" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "SpecimenDescriptionTrial"
    },
    "00400554" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "SpecimenUID"
    },
    "00400555" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AcquisitionContextSequence"
    },
    "00400556" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "AcquisitionContextDescription"
    },
    "0040059A" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SpecimenTypeCodeSequence"
    },
    "00400560" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SpecimenDescriptionSequence"
    },
    "00400562" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IssuerOfTheSpecimenIdentifierSequence"
    },
    "00400600" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SpecimenShortDescription"
    },
    "00400602" : {
        "VR" : "UT",
        "VM" : "1",
        "KEYWORD" : "SpecimenDetailedDescription"
    },
    "00400610" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SpecimenPreparationSequence"
    },
    "00400612" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SpecimenPreparationStepContentItemSequence"
    },
    "00400620" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SpecimenLocalizationContentItemSequence"
    },
    "004006FA" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SlideIdentifier"
    },
    "0040071A" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ImageCenterPointCoordinatesSequence"
    },
    "0040072A" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "XOffsetInSlideCoordinateSystem"
    },
    "0040073A" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "YOffsetInSlideCoordinateSystem"
    },
    "0040074A" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ZOffsetInSlideCoordinateSystem"
    },
    "004008D8" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PixelSpacingSequence"
    },
    "004008DA" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CoordinateSystemAxisCodeSequence"
    },
    "004008EA" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MeasurementUnitsCodeSequence"
    },
    "004009F8" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "VitalStainCodeSequenceTrial"
    },
    "00401001" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "RequestedProcedureID"
    },
    "00401002" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ReasonForTheRequestedProcedure"
    },
    "00401003" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "RequestedProcedurePriority"
    },
    "00401004" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PatientTransportArrangements"
    },
    "00401005" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "RequestedProcedureLocation"
    },
    "00401006" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "PlacerOrderNumberProcedure"
    },
    "00401007" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "FillerOrderNumberProcedure"
    },
    "00401008" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ConfidentialityCode"
    },
    "00401009" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "ReportingPriority"
    },
    "0040100A" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReasonForRequestedProcedureCodeSequence"
    },
    "00401010" : {
        "VR" : "PN",
        "VM" : "1-n",
        "KEYWORD" : "NamesOfIntendedRecipientsOfResults"
    },
    "00401011" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IntendedRecipientsOfResultsIdentificationSequence"
    },
    "00401012" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReasonForPerformedProcedureCodeSequence"
    },
    "00401060" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "RequestedProcedureDescriptionTrial"
    },
    "00401101" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PersonIdentificationCodeSequence"
    },
    "00401102" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "PersonAddress"
    },
    "00401103" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "PersonTelephoneNumbers"
    },
    "00401400" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "RequestedProcedureComments"
    },
    "00402001" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ReasonForTheImagingServiceRequest"
    },
    "00402004" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "IssueDateOfImagingServiceRequest"
    },
    "00402005" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "IssueTimeOfImagingServiceRequest"
    },
    "00402006" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "PlacerOrderNumberImagingServiceRequestRetired"
    },
    "00402007" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "FillerOrderNumberImagingServiceRequestRetired"
    },
    "00402008" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "OrderEnteredBy"
    },
    "00402009" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "OrderEntererLocation"
    },
    "00402010" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "OrderCallbackPhoneNumber"
    },
    "00402016" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PlacerOrderNumberImagingServiceRequest"
    },
    "00402017" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "FillerOrderNumberImagingServiceRequest"
    },
    "00402400" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "ImagingServiceRequestComments"
    },
    "00403001" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ConfidentialityConstraintOnPatientDataDescription"
    },
    "00404001" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "GeneralPurposeScheduledProcedureStepStatus"
    },
    "00404002" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "GeneralPurposePerformedProcedureStepStatus"
    },
    "00404003" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "GeneralPurposeScheduledProcedureStepPriority"
    },
    "00404004" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ScheduledProcessingApplicationsCodeSequence"
    },
    "00404005" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "ScheduledProcedureStepStartDateTime"
    },
    "00404006" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "MultipleCopiesFlag"
    },
    "00404007" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PerformedProcessingApplicationsCodeSequence"
    },
    "00404009" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "HumanPerformerCodeSequence"
    },
    "00404010" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "ScheduledProcedureStepModificationDateTime"
    },
    "00404011" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "ExpectedCompletionDateTime"
    },
    "00404015" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ResultingGeneralPurposePerformedProcedureStepsSequence"
    },
    "00404016" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedGeneralPurposeScheduledProcedureStepSequence"
    },
    "00404018" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ScheduledWorkitemCodeSequence"
    },
    "00404019" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PerformedWorkitemCodeSequence"
    },
    "00404020" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "InputAvailabilityFlag"
    },
    "00404021" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "InputInformationSequence"
    },
    "00404022" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RelevantInformationSequence"
    },
    "00404023" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "ReferencedGeneralPurposeScheduledProcedureStepTransactionUID"
    },
    "00404025" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ScheduledStationNameCodeSequence"
    },
    "00404026" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ScheduledStationClassCodeSequence"
    },
    "00404027" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ScheduledStationGeographicLocationCodeSequence"
    },
    "00404028" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PerformedStationNameCodeSequence"
    },
    "00404029" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PerformedStationClassCodeSequence"
    },
    "00404030" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PerformedStationGeographicLocationCodeSequence"
    },
    "00404031" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RequestedSubsequentWorkitemCodeSequence"
    },
    "00404032" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "NonDICOMOutputCodeSequence"
    },
    "00404033" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OutputInformationSequence"
    },
    "00404034" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ScheduledHumanPerformersSequence"
    },
    "00404035" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ActualHumanPerformersSequence"
    },
    "00404036" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "HumanPerformerOrganization"
    },
    "00404037" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "HumanPerformerName"
    },
    "00404040" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RawDataHandling"
    },
    "00404041" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "InputReadinessState"
    },
    "00404050" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "PerformedProcedureStepStartDateTime"
    },
    "00404051" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "PerformedProcedureStepEndDateTime"
    },
    "00404052" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "ProcedureStepCancellationDateTime"
    },
    "00408302" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "EntranceDoseInmGy"
    },
    "00409094" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedImageRealWorldValueMappingSequence"
    },
    "00409096" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RealWorldValueMappingSequence"
    },
    "00409098" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PixelValueMappingCodeSequence"
    },
    "00409210" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "LUTLabel"
    },
    "00409211" : {
        "VR" : "US|SS",
        "VM" : "1",
        "KEYWORD" : "RealWorldValueLastValueMapped"
    },
    "00409212" : {
        "VR" : "FD",
        "VM" : "1-n",
        "KEYWORD" : "RealWorldValueLUTData"
    },
    "00409216" : {
        "VR" : "US|SS",
        "VM" : "1",
        "KEYWORD" : "RealWorldValueFirstValueMapped"
    },
    "00409224" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "RealWorldValueIntercept"
    },
    "00409225" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "RealWorldValueSlope"
    },
    "0040A007" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FindingsFlagTrial"
    },
    "0040A010" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RelationshipType"
    },
    "0040A020" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FindingsSequenceTrial"
    },
    "0040A021" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "FindingsGroupUIDTrial"
    },
    "0040A022" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "ReferencedFindingsGroupUIDTrial"
    },
    "0040A023" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "FindingsGroupRecordingDateTrial"
    },
    "0040A024" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "FindingsGroupRecordingTimeTrial"
    },
    "0040A026" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FindingsSourceCategoryCodeSequenceTrial"
    },
    "0040A027" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "VerifyingOrganization"
    },
    "0040A028" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DocumentingOrganizationIdentifierCodeSequenceTrial"
    },
    "0040A030" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "VerificationDateTime"
    },
    "0040A032" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "ObservationDateTime"
    },
    "0040A040" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ValueType"
    },
    "0040A043" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ConceptNameCodeSequence"
    },
    "0040A047" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "MeasurementPrecisionDescriptionTrial"
    },
    "0040A050" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ContinuityOfContent"
    },
    "0040A057" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "UrgencyOrPriorityAlertsTrial"
    },
    "0040A060" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SequencingIndicatorTrial"
    },
    "0040A066" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DocumentIdentifierCodeSequenceTrial"
    },
    "0040A067" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "DocumentAuthorTrial"
    },
    "0040A068" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DocumentAuthorIdentifierCodeSequenceTrial"
    },
    "0040A070" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IdentifierCodeSequenceTrial"
    },
    "0040A073" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "VerifyingObserverSequence"
    },
    "0040A074" : {
        "VR" : "OB",
        "VM" : "1",
        "KEYWORD" : "ObjectBinaryIdentifierTrial"
    },
    "0040A075" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "VerifyingObserverName"
    },
    "0040A076" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DocumentingObserverIdentifierCodeSequenceTrial"
    },
    "0040A078" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AuthorObserverSequence"
    },
    "0040A07A" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ParticipantSequence"
    },
    "0040A07C" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CustodialOrganizationSequence"
    },
    "0040A080" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ParticipationType"
    },
    "0040A082" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "ParticipationDateTime"
    },
    "0040A084" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ObserverType"
    },
    "0040A085" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ProcedureIdentifierCodeSequenceTrial"
    },
    "0040A088" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "VerifyingObserverIdentificationCodeSequence"
    },
    "0040A089" : {
        "VR" : "OB",
        "VM" : "1",
        "KEYWORD" : "ObjectDirectoryBinaryIdentifierTrial"
    },
    "0040A090" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "EquivalentCDADocumentSequence"
    },
    "0040A0B0" : {
        "VR" : "US",
        "VM" : "2-2n",
        "KEYWORD" : "ReferencedWaveformChannels"
    },
    "0040A110" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "DateOfDocumentOrVerbalTransactionTrial"
    },
    "0040A112" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "TimeOfDocumentCreationOrVerbalTransactionTrial"
    },
    "0040A120" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "DateTime"
    },
    "0040A121" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "Date"
    },
    "0040A122" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "Time"
    },
    "0040A123" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "PersonName"
    },
    "0040A124" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "UID"
    },
    "0040A125" : {
        "VR" : "CS",
        "VM" : "2",
        "KEYWORD" : "ReportStatusIDTrial"
    },
    "0040A130" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TemporalRangeType"
    },
    "0040A132" : {
        "VR" : "UL",
        "VM" : "1-n",
        "KEYWORD" : "ReferencedSamplePositions"
    },
    "0040A136" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "ReferencedFrameNumbers"
    },
    "0040A138" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "ReferencedTimeOffsets"
    },
    "0040A13A" : {
        "VR" : "DT",
        "VM" : "1-n",
        "KEYWORD" : "ReferencedDateTime"
    },
    "0040A160" : {
        "VR" : "UT",
        "VM" : "1",
        "KEYWORD" : "TextValue"
    },
    "0040A167" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ObservationCategoryCodeSequenceTrial"
    },
    "0040A168" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ConceptCodeSequence"
    },
    "0040A16A" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "BibliographicCitationTrial"
    },
    "0040A170" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PurposeOfReferenceCodeSequence"
    },
    "0040A171" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "ObservationUIDTrial"
    },
    "0040A172" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "ReferencedObservationUIDTrial"
    },
    "0040A173" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ReferencedObservationClassTrial"
    },
    "0040A174" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ReferencedObjectObservationClassTrial"
    },
    "0040A180" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "AnnotationGroupNumber"
    },
    "0040A192" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "ObservationDateTrial"
    },
    "0040A193" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "ObservationTimeTrial"
    },
    "0040A194" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "MeasurementAutomationTrial"
    },
    "0040A195" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ModifierCodeSequence"
    },
    "0040A224" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "IdentificationDescriptionTrial"
    },
    "0040A290" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CoordinatesSetGeometricTypeTrial"
    },
    "0040A296" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AlgorithmCodeSequenceTrial"
    },
    "0040A297" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "AlgorithmDescriptionTrial"
    },
    "0040A29A" : {
        "VR" : "SL",
        "VM" : "2-2n",
        "KEYWORD" : "PixelCoordinatesSetTrial"
    },
    "0040A300" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MeasuredValueSequence"
    },
    "0040A301" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "NumericValueQualifierCodeSequence"
    },
    "0040A307" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "CurrentObserverTrial"
    },
    "0040A30A" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "NumericValue"
    },
    "0040A313" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedAccessionSequenceTrial"
    },
    "0040A33A" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "ReportStatusCommentTrial"
    },
    "0040A340" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ProcedureContextSequenceTrial"
    },
    "0040A352" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "VerbalSourceTrial"
    },
    "0040A353" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "AddressTrial"
    },
    "0040A354" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "TelephoneNumberTrial"
    },
    "0040A358" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "VerbalSourceIdentifierCodeSequenceTrial"
    },
    "0040A360" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PredecessorDocumentsSequence"
    },
    "0040A370" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedRequestSequence"
    },
    "0040A372" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PerformedProcedureCodeSequence"
    },
    "0040A375" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CurrentRequestedProcedureEvidenceSequence"
    },
    "0040A380" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReportDetailSequenceTrial"
    },
    "0040A385" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PertinentOtherEvidenceSequence"
    },
    "0040A390" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "HL7StructuredDocumentReferenceSequence"
    },
    "0040A402" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "ObservationSubjectUIDTrial"
    },
    "0040A403" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ObservationSubjectClassTrial"
    },
    "0040A404" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ObservationSubjectTypeCodeSequenceTrial"
    },
    "0040A491" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CompletionFlag"
    },
    "0040A492" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "CompletionFlagDescription"
    },
    "0040A493" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "VerificationFlag"
    },
    "0040A494" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ArchiveRequested"
    },
    "0040A496" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PreliminaryFlag"
    },
    "0040A504" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ContentTemplateSequence"
    },
    "0040A525" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IdenticalDocumentsSequence"
    },
    "0040A600" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ObservationSubjectContextFlagTrial"
    },
    "0040A601" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ObserverContextFlagTrial"
    },
    "0040A603" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ProcedureContextFlagTrial"
    },
    "0040A730" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ContentSequence"
    },
    "0040A731" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RelationshipSequenceTrial"
    },
    "0040A732" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RelationshipTypeCodeSequenceTrial"
    },
    "0040A744" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "LanguageCodeSequenceTrial"
    },
    "0040A992" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "UniformResourceLocatorTrial"
    },
    "0040B020" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "WaveformAnnotationSequence"
    },
    "0040DB00" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TemplateIdentifier"
    },
    "0040DB06" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "TemplateVersion"
    },
    "0040DB07" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "TemplateLocalVersion"
    },
    "0040DB0B" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TemplateExtensionFlag"
    },
    "0040DB0C" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "TemplateExtensionOrganizationUID"
    },
    "0040DB0D" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "TemplateExtensionCreatorUID"
    },
    "0040DB73" : {
        "VR" : "UL",
        "VM" : "1-n",
        "KEYWORD" : "ReferencedContentItemIdentifier"
    },
    "0040E001" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "HL7InstanceIdentifier"
    },
    "0040E004" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "HL7DocumentEffectiveTime"
    },
    "0040E006" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "HL7DocumentTypeCodeSequence"
    },
    "0040E008" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DocumentClassCodeSequence"
    },
    "0040E010" : {
        "VR" : "UT",
        "VM" : "1",
        "KEYWORD" : "RetrieveURI"
    },
    "0040E011" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "RetrieveLocationUID"
    },
    "0040E020" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TypeOfInstances"
    },
    "0040E021" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DICOMRetrievalSequence"
    },
    "0040E022" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DICOMMediaRetrievalSequence"
    },
    "0040E023" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "WADORetrievalSequence"
    },
    "0040E024" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "XDSRetrievalSequence"
    },
    "0040E030" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "RepositoryUniqueID"
    },
    "0040E031" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "HomeCommunityID"
    },
    "00420010" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "DocumentTitle"
    },
    "00420011" : {
        "VR" : "OB",
        "VM" : "1",
        "KEYWORD" : "EncapsulatedDocument"
    },
    "00420012" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "MIMETypeOfEncapsulatedDocument"
    },
    "00420013" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SourceInstanceSequence"
    },
    "00420014" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "ListOfMIMETypes"
    },
    "00440001" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "ProductPackageIdentifier"
    },
    "00440002" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SubstanceAdministrationApproval"
    },
    "00440003" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "ApprovalStatusFurtherDescription"
    },
    "00440004" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "ApprovalStatusDateTime"
    },
    "00440007" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ProductTypeCodeSequence"
    },
    "00440008" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "ProductName"
    },
    "00440009" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "ProductDescription"
    },
    "0044000A" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ProductLotIdentifier"
    },
    "0044000B" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "ProductExpirationDateTime"
    },
    "00440010" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "SubstanceAdministrationDateTime"
    },
    "00440011" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SubstanceAdministrationNotes"
    },
    "00440012" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SubstanceAdministrationDeviceID"
    },
    "00440013" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ProductParameterSequence"
    },
    "00440019" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SubstanceAdministrationParameterSequence"
    },
    "00460012" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "LensDescription"
    },
    "00460014" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RightLensSequence"
    },
    "00460015" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "LeftLensSequence"
    },
    "00460016" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "UnspecifiedLateralityLensSequence"
    },
    "00460018" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CylinderSequence"
    },
    "00460028" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PrismSequence"
    },
    "00460030" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "HorizontalPrismPower"
    },
    "00460032" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "HorizontalPrismBase"
    },
    "00460034" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "VerticalPrismPower"
    },
    "00460036" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "VerticalPrismBase"
    },
    "00460038" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "LensSegmentType"
    },
    "00460040" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "OpticalTransmittance"
    },
    "00460042" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ChannelWidth"
    },
    "00460044" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "PupilSize"
    },
    "00460046" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "CornealSize"
    },
    "00460050" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AutorefractionRightEyeSequence"
    },
    "00460052" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AutorefractionLeftEyeSequence"
    },
    "00460060" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "DistancePupillaryDistance"
    },
    "00460062" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "NearPupillaryDistance"
    },
    "00460063" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "IntermediatePupillaryDistance"
    },
    "00460064" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "OtherPupillaryDistance"
    },
    "00460070" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "KeratometryRightEyeSequence"
    },
    "00460071" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "KeratometryLeftEyeSequence"
    },
    "00460074" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SteepKeratometricAxisSequence"
    },
    "00460075" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "RadiusOfCurvature"
    },
    "00460076" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "KeratometricPower"
    },
    "00460077" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "KeratometricAxis"
    },
    "00460080" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FlatKeratometricAxisSequence"
    },
    "00460092" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BackgroundColor"
    },
    "00460094" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "Optotype"
    },
    "00460095" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OptotypePresentation"
    },
    "00460097" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SubjectiveRefractionRightEyeSequence"
    },
    "00460098" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SubjectiveRefractionLeftEyeSequence"
    },
    "00460100" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AddNearSequence"
    },
    "00460101" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AddIntermediateSequence"
    },
    "00460102" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AddOtherSequence"
    },
    "00460104" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "AddPower"
    },
    "00460106" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ViewingDistance"
    },
    "00460121" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "VisualAcuityTypeCodeSequence"
    },
    "00460122" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "VisualAcuityRightEyeSequence"
    },
    "00460123" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "VisualAcuityLeftEyeSequence"
    },
    "00460124" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "VisualAcuityBothEyesOpenSequence"
    },
    "00460125" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ViewingDistanceType"
    },
    "00460135" : {
        "VR" : "SS",
        "VM" : "2",
        "KEYWORD" : "VisualAcuityModifiers"
    },
    "00460137" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "DecimalVisualAcuity"
    },
    "00460139" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "OptotypeDetailedDefinition"
    },
    "00460145" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedRefractiveMeasurementsSequence"
    },
    "00460146" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "SpherePower"
    },
    "00460147" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "CylinderPower"
    },
    "00480001" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "ImagedVolumeWidth"
    },
    "00480002" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "ImagedVolumeHeight"
    },
    "00480003" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "ImagedVolumeDepth"
    },
    "00480006" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "TotalPixelMatrixColumns"
    },
    "00480007" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "TotalPixelMatrixRows"
    },
    "00480008" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TotalPixelMatrixOriginSequence"
    },
    "00480010" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SpecimenLabelInImage"
    },
    "00480011" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FocusMethod"
    },
    "00480012" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ExtendedDepthOfField"
    },
    "00480013" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfFocalPlanes"
    },
    "00480014" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "DistanceBetweenFocalPlanes"
    },
    "00480015" : {
        "VR" : "US",
        "VM" : "3",
        "KEYWORD" : "RecommendedAbsentPixelCIELabValue"
    },
    "00480100" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IlluminatorTypeCodeSequence"
    },
    "00480102" : {
        "VR" : "DS",
        "VM" : "6",
        "KEYWORD" : "ImageOrientationSlide"
    },
    "00480105" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OpticalPathSequence"
    },
    "00480106" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "OpticalPathIdentifier"
    },
    "00480107" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "OpticalPathDescription"
    },
    "00480108" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IlluminationColorCodeSequence"
    },
    "00480110" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SpecimenReferenceSequence"
    },
    "00480111" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "CondenserLensPower"
    },
    "00480112" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ObjectiveLensPower"
    },
    "00480113" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ObjectiveLensNumericalAperture"
    },
    "00480120" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PaletteColorLookupTableSequence"
    },
    "00480200" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedImageNavigationSequence"
    },
    "00480201" : {
        "VR" : "US",
        "VM" : "2",
        "KEYWORD" : "TopLeftHandCornerOfLocalizerArea"
    },
    "00480202" : {
        "VR" : "US",
        "VM" : "2",
        "KEYWORD" : "BottomRightHandCornerOfLocalizerArea"
    },
    "00480207" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OpticalPathIdentificationSequence"
    },
    "0048021A" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PlanePositionSlideSequence"
    },
    "0048021E" : {
        "VR" : "SL",
        "VM" : "1",
        "KEYWORD" : "RowPositionInTotalImagePixelMatrix"
    },
    "0048021F" : {
        "VR" : "SL",
        "VM" : "1",
        "KEYWORD" : "ColumnPositionInTotalImagePixelMatrix"
    },
    "00480301" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PixelOriginInterpretation"
    },
    "00500004" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CalibrationImage"
    },
    "00500010" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DeviceSequence"
    },
    "00500012" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ContainerComponentTypeCodeSequence"
    },
    "00500013" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ContainerComponentThickness"
    },
    "00500014" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DeviceLength"
    },
    "00500015" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ContainerComponentWidth"
    },
    "00500016" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DeviceDiameter"
    },
    "00500017" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DeviceDiameterUnits"
    },
    "00500018" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DeviceVolume"
    },
    "00500019" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "InterMarkerDistance"
    },
    "0050001A" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ContainerComponentMaterial"
    },
    "0050001B" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ContainerComponentID"
    },
    "0050001C" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ContainerComponentLength"
    },
    "0050001D" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ContainerComponentDiameter"
    },
    "0050001E" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ContainerComponentDescription"
    },
    "00500020" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "DeviceDescription"
    },
    "00520001" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "ContrastBolusIngredientPercentByVolume"
    },
    "00520002" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "OCTFocalDistance"
    },
    "00520003" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "BeamSpotSize"
    },
    "00520004" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "EffectiveRefractiveIndex"
    },
    "00520006" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OCTAcquisitionDomain"
    },
    "00520007" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "OCTOpticalCenterWavelength"
    },
    "00520008" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "AxialResolution"
    },
    "00520009" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "RangingDepth"
    },
    "00520011" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ALineRate"
    },
    "00520012" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ALinesPerFrame"
    },
    "00520013" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "CatheterRotationalRate"
    },
    "00520014" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ALinePixelSpacing"
    },
    "00520016" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ModeOfPercutaneousAccessSequence"
    },
    "00520025" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IntravascularOCTFrameTypeSequence"
    },
    "00520026" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OCTZOffsetApplied"
    },
    "00520027" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IntravascularFrameContentSequence"
    },
    "00520028" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "IntravascularLongitudinalDistance"
    },
    "00520029" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IntravascularOCTFrameContentSequence"
    },
    "00520030" : {
        "VR" : "SS",
        "VM" : "1",
        "KEYWORD" : "OCTZOffsetCorrection"
    },
    "00520031" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CatheterDirectionOfRotation"
    },
    "00520033" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "SeamLineLocation"
    },
    "00520034" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "FirstALineLocation"
    },
    "00520036" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "SeamLineIndex"
    },
    "00520038" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfPaddedAlines"
    },
    "00520039" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "InterpolationType"
    },
    "0052003A" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RefractiveIndexApplied"
    },
    "00540010" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "EnergyWindowVector"
    },
    "00540011" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfEnergyWindows"
    },
    "00540012" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "EnergyWindowInformationSequence"
    },
    "00540013" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "EnergyWindowRangeSequence"
    },
    "00540014" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "EnergyWindowLowerLimit"
    },
    "00540015" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "EnergyWindowUpperLimit"
    },
    "00540016" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RadiopharmaceuticalInformationSequence"
    },
    "00540017" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ResidualSyringeCounts"
    },
    "00540018" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "EnergyWindowName"
    },
    "00540020" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "DetectorVector"
    },
    "00540021" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfDetectors"
    },
    "00540022" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DetectorInformationSequence"
    },
    "00540030" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "PhaseVector"
    },
    "00540031" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfPhases"
    },
    "00540032" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PhaseInformationSequence"
    },
    "00540033" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfFramesInPhase"
    },
    "00540036" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "PhaseDelay"
    },
    "00540038" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "PauseBetweenFrames"
    },
    "00540039" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PhaseDescription"
    },
    "00540050" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "RotationVector"
    },
    "00540051" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfRotations"
    },
    "00540052" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RotationInformationSequence"
    },
    "00540053" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfFramesInRotation"
    },
    "00540060" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "RRIntervalVector"
    },
    "00540061" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfRRIntervals"
    },
    "00540062" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "GatedInformationSequence"
    },
    "00540063" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DataInformationSequence"
    },
    "00540070" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "TimeSlotVector"
    },
    "00540071" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfTimeSlots"
    },
    "00540072" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TimeSlotInformationSequence"
    },
    "00540073" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TimeSlotTime"
    },
    "00540080" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "SliceVector"
    },
    "00540081" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfSlices"
    },
    "00540090" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "AngularViewVector"
    },
    "00540100" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "TimeSliceVector"
    },
    "00540101" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfTimeSlices"
    },
    "00540200" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "StartAngle"
    },
    "00540202" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TypeOfDetectorMotion"
    },
    "00540210" : {
        "VR" : "IS",
        "VM" : "1-n",
        "KEYWORD" : "TriggerVector"
    },
    "00540211" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfTriggersInPhase"
    },
    "00540220" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ViewCodeSequence"
    },
    "00540222" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ViewModifierCodeSequence"
    },
    "00540300" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RadionuclideCodeSequence"
    },
    "00540302" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AdministrationRouteCodeSequence"
    },
    "00540304" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RadiopharmaceuticalCodeSequence"
    },
    "00540306" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CalibrationDataSequence"
    },
    "00540308" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "EnergyWindowNumber"
    },
    "00540400" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "ImageID"
    },
    "00540410" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PatientOrientationCodeSequence"
    },
    "00540412" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PatientOrientationModifierCodeSequence"
    },
    "00540414" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PatientGantryRelationshipCodeSequence"
    },
    "00540500" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SliceProgressionDirection"
    },
    "00541000" : {
        "VR" : "CS",
        "VM" : "2",
        "KEYWORD" : "SeriesType"
    },
    "00541001" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "Units"
    },
    "00541002" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CountsSource"
    },
    "00541004" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ReprojectionMethod"
    },
    "00541006" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SUVType"
    },
    "00541100" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RandomsCorrectionMethod"
    },
    "00541101" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "AttenuationCorrectionMethod"
    },
    "00541102" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DecayCorrection"
    },
    "00541103" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ReconstructionMethod"
    },
    "00541104" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "DetectorLinesOfResponseUsed"
    },
    "00541105" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ScatterCorrectionMethod"
    },
    "00541200" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "AxialAcceptance"
    },
    "00541201" : {
        "VR" : "IS",
        "VM" : "2",
        "KEYWORD" : "AxialMash"
    },
    "00541202" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "TransverseMash"
    },
    "00541203" : {
        "VR" : "DS",
        "VM" : "2",
        "KEYWORD" : "DetectorElementSize"
    },
    "00541210" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "CoincidenceWindowWidth"
    },
    "00541220" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "SecondaryCountsType"
    },
    "00541300" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "FrameReferenceTime"
    },
    "00541310" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "PrimaryPromptsCountsAccumulated"
    },
    "00541311" : {
        "VR" : "IS",
        "VM" : "1-n",
        "KEYWORD" : "SecondaryCountsAccumulated"
    },
    "00541320" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SliceSensitivityFactor"
    },
    "00541321" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DecayFactor"
    },
    "00541322" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DoseCalibrationFactor"
    },
    "00541323" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ScatterFractionFactor"
    },
    "00541324" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DeadTimeFactor"
    },
    "00541330" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ImageIndex"
    },
    "00541400" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "CountsIncluded"
    },
    "00541401" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DeadTimeCorrectionFlag"
    },
    "00603000" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "HistogramSequence"
    },
    "00603002" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "HistogramNumberOfBins"
    },
    "00603004" : {
        "VR" : "US|SS",
        "VM" : "1",
        "KEYWORD" : "HistogramFirstBinValue"
    },
    "00603006" : {
        "VR" : "US|SS",
        "VM" : "1",
        "KEYWORD" : "HistogramLastBinValue"
    },
    "00603008" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "HistogramBinWidth"
    },
    "00603010" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "HistogramExplanation"
    },
    "00603020" : {
        "VR" : "UL",
        "VM" : "1-n",
        "KEYWORD" : "HistogramData"
    },
    "00620001" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SegmentationType"
    },
    "00620002" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SegmentSequence"
    },
    "00620003" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SegmentedPropertyCategoryCodeSequence"
    },
    "00620004" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "SegmentNumber"
    },
    "00620005" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SegmentLabel"
    },
    "00620006" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "SegmentDescription"
    },
    "00620008" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SegmentAlgorithmType"
    },
    "00620009" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SegmentAlgorithmName"
    },
    "0062000A" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SegmentIdentificationSequence"
    },
    "0062000B" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "ReferencedSegmentNumber"
    },
    "0062000C" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "RecommendedDisplayGrayscaleValue"
    },
    "0062000D" : {
        "VR" : "US",
        "VM" : "3",
        "KEYWORD" : "RecommendedDisplayCIELabValue"
    },
    "0062000E" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "MaximumFractionalValue"
    },
    "0062000F" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SegmentedPropertyTypeCodeSequence"
    },
    "00620010" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SegmentationFractionalType"
    },
    "00640002" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DeformableRegistrationSequence"
    },
    "00640003" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "SourceFrameOfReferenceUID"
    },
    "00640005" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DeformableRegistrationGridSequence"
    },
    "00640007" : {
        "VR" : "UL",
        "VM" : "3",
        "KEYWORD" : "GridDimensions"
    },
    "00640008" : {
        "VR" : "FD",
        "VM" : "3",
        "KEYWORD" : "GridResolution"
    },
    "00640009" : {
        "VR" : "OF",
        "VM" : "1",
        "KEYWORD" : "VectorGridData"
    },
    "0064000F" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PreDeformationMatrixRegistrationSequence"
    },
    "00640010" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PostDeformationMatrixRegistrationSequence"
    },
    "00660001" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "NumberOfSurfaces"
    },
    "00660002" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SurfaceSequence"
    },
    "00660003" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "SurfaceNumber"
    },
    "00660004" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "SurfaceComments"
    },
    "00660009" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SurfaceProcessing"
    },
    "0066000A" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "SurfaceProcessingRatio"
    },
    "0066000B" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SurfaceProcessingDescription"
    },
    "0066000C" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "RecommendedPresentationOpacity"
    },
    "0066000D" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RecommendedPresentationType"
    },
    "0066000E" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FiniteVolume"
    },
    "00660010" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "Manifold"
    },
    "00660011" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SurfacePointsSequence"
    },
    "00660012" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SurfacePointsNormalsSequence"
    },
    "00660013" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SurfaceMeshPrimitivesSequence"
    },
    "00660015" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "NumberOfSurfacePoints"
    },
    "00660016" : {
        "VR" : "OF",
        "VM" : "1",
        "KEYWORD" : "PointCoordinatesData"
    },
    "00660017" : {
        "VR" : "FL",
        "VM" : "3",
        "KEYWORD" : "PointPositionAccuracy"
    },
    "00660018" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "MeanPointDistance"
    },
    "00660019" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "MaximumPointDistance"
    },
    "0066001A" : {
        "VR" : "FL",
        "VM" : "6",
        "KEYWORD" : "PointsBoundingBoxCoordinates"
    },
    "0066001B" : {
        "VR" : "FL",
        "VM" : "3",
        "KEYWORD" : "AxisOfRotation"
    },
    "0066001C" : {
        "VR" : "FL",
        "VM" : "3",
        "KEYWORD" : "CenterOfRotation"
    },
    "0066001E" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "NumberOfVectors"
    },
    "0066001F" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "VectorDimensionality"
    },
    "00660020" : {
        "VR" : "FL",
        "VM" : "1-n",
        "KEYWORD" : "VectorAccuracy"
    },
    "00660021" : {
        "VR" : "OF",
        "VM" : "1",
        "KEYWORD" : "VectorCoordinateData"
    },
    "00660023" : {
        "VR" : "OW",
        "VM" : "1",
        "KEYWORD" : "TrianglePointIndexList"
    },
    "00660024" : {
        "VR" : "OW",
        "VM" : "1",
        "KEYWORD" : "EdgePointIndexList"
    },
    "00660025" : {
        "VR" : "OW",
        "VM" : "1",
        "KEYWORD" : "VertexPointIndexList"
    },
    "00660026" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TriangleStripSequence"
    },
    "00660027" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TriangleFanSequence"
    },
    "00660028" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "LineSequence"
    },
    "00660029" : {
        "VR" : "OW",
        "VM" : "1",
        "KEYWORD" : "PrimitivePointIndexList"
    },
    "0066002A" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "SurfaceCount"
    },
    "0066002B" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedSurfaceSequence"
    },
    "0066002C" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "ReferencedSurfaceNumber"
    },
    "0066002D" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SegmentSurfaceGenerationAlgorithmIdentificationSequence"
    },
    "0066002E" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SegmentSurfaceSourceInstanceSequence"
    },
    "0066002F" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AlgorithmFamilyCodeSequence"
    },
    "00660030" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AlgorithmNameCodeSequence"
    },
    "00660031" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "AlgorithmVersion"
    },
    "00660032" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "AlgorithmParameters"
    },
    "00660034" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FacetSequence"
    },
    "00660035" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SurfaceProcessingAlgorithmIdentificationSequence"
    },
    "00660036" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "AlgorithmName"
    },
    "00686210" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ImplantSize"
    },
    "00686221" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ImplantTemplateVersion"
    },
    "00686222" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReplacedImplantTemplateSequence"
    },
    "00686223" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ImplantType"
    },
    "00686224" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DerivationImplantTemplateSequence"
    },
    "00686225" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OriginalImplantTemplateSequence"
    },
    "00686226" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "EffectiveDateTime"
    },
    "00686230" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ImplantTargetAnatomySequence"
    },
    "00686260" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "InformationFromManufacturerSequence"
    },
    "00686265" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "NotificationFromManufacturerSequence"
    },
    "00686270" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "InformationIssueDateTime"
    },
    "00686280" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "InformationSummary"
    },
    "006862A0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ImplantRegulatoryDisapprovalCodeSequence"
    },
    "006862A5" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "OverallTemplateSpatialTolerance"
    },
    "006862C0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "HPGLDocumentSequence"
    },
    "006862D0" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "HPGLDocumentID"
    },
    "006862D5" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "HPGLDocumentLabel"
    },
    "006862E0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ViewOrientationCodeSequence"
    },
    "006862F0" : {
        "VR" : "FD",
        "VM" : "9",
        "KEYWORD" : "ViewOrientationModifier"
    },
    "006862F2" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "HPGLDocumentScaling"
    },
    "00686300" : {
        "VR" : "OB",
        "VM" : "1",
        "KEYWORD" : "HPGLDocument"
    },
    "00686310" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "HPGLContourPenNumber"
    },
    "00686320" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "HPGLPenSequence"
    },
    "00686330" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "HPGLPenNumber"
    },
    "00686340" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "HPGLPenLabel"
    },
    "00686345" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "HPGLPenDescription"
    },
    "00686346" : {
        "VR" : "FD",
        "VM" : "2",
        "KEYWORD" : "RecommendedRotationPoint"
    },
    "00686347" : {
        "VR" : "FD",
        "VM" : "4",
        "KEYWORD" : "BoundingRectangle"
    },
    "00686350" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "ImplantTemplate3DModelSurfaceNumber"
    },
    "00686360" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SurfaceModelDescriptionSequence"
    },
    "00686380" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SurfaceModelLabel"
    },
    "00686390" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "SurfaceModelScalingFactor"
    },
    "006863A0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MaterialsCodeSequence"
    },
    "006863A4" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CoatingMaterialsCodeSequence"
    },
    "006863A8" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ImplantTypeCodeSequence"
    },
    "006863AC" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FixationMethodCodeSequence"
    },
    "006863B0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MatingFeatureSetsSequence"
    },
    "006863C0" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "MatingFeatureSetID"
    },
    "006863D0" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "MatingFeatureSetLabel"
    },
    "006863E0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MatingFeatureSequence"
    },
    "006863F0" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "MatingFeatureID"
    },
    "00686400" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MatingFeatureDegreeOfFreedomSequence"
    },
    "00686410" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "DegreeOfFreedomID"
    },
    "00686420" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DegreeOfFreedomType"
    },
    "00686430" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TwoDMatingFeatureCoordinatesSequence"
    },
    "00686440" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ReferencedHPGLDocumentID"
    },
    "00686450" : {
        "VR" : "FD",
        "VM" : "2",
        "KEYWORD" : "TwoDMatingPoint"
    },
    "00686460" : {
        "VR" : "FD",
        "VM" : "4",
        "KEYWORD" : "TwoDMatingAxes"
    },
    "00686470" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TwoDDegreeOfFreedomSequence"
    },
    "00686490" : {
        "VR" : "FD",
        "VM" : "3",
        "KEYWORD" : "ThreeDDegreeOfFreedomAxis"
    },
    "006864A0" : {
        "VR" : "FD",
        "VM" : "2",
        "KEYWORD" : "RangeOfFreedom"
    },
    "006864C0" : {
        "VR" : "FD",
        "VM" : "3",
        "KEYWORD" : "ThreeDMatingPoint"
    },
    "006864D0" : {
        "VR" : "FD",
        "VM" : "9",
        "KEYWORD" : "ThreeDMatingAxes"
    },
    "006864F0" : {
        "VR" : "FD",
        "VM" : "3",
        "KEYWORD" : "TwoDDegreeOfFreedomAxis"
    },
    "00686500" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PlanningLandmarkPointSequence"
    },
    "00686510" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PlanningLandmarkLineSequence"
    },
    "00686520" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PlanningLandmarkPlaneSequence"
    },
    "00686530" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "PlanningLandmarkID"
    },
    "00686540" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PlanningLandmarkDescription"
    },
    "00686545" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PlanningLandmarkIdentificationCodeSequence"
    },
    "00686550" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TwoDPointCoordinatesSequence"
    },
    "00686560" : {
        "VR" : "FD",
        "VM" : "2",
        "KEYWORD" : "TwoDPointCoordinates"
    },
    "00686590" : {
        "VR" : "FD",
        "VM" : "3",
        "KEYWORD" : "ThreeDPointCoordinates"
    },
    "006865A0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TwoDLineCoordinatesSequence"
    },
    "006865B0" : {
        "VR" : "FD",
        "VM" : "4",
        "KEYWORD" : "TwoDLineCoordinates"
    },
    "006865D0" : {
        "VR" : "FD",
        "VM" : "6",
        "KEYWORD" : "ThreeDLineCoordinates"
    },
    "006865E0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TwoDPlaneCoordinatesSequence"
    },
    "006865F0" : {
        "VR" : "FD",
        "VM" : "4",
        "KEYWORD" : "TwoDPlaneIntersection"
    },
    "00686610" : {
        "VR" : "FD",
        "VM" : "3",
        "KEYWORD" : "ThreeDPlaneOrigin"
    },
    "00686620" : {
        "VR" : "FD",
        "VM" : "3",
        "KEYWORD" : "ThreeDPlaneNormal"
    },
    "00700001" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "GraphicAnnotationSequence"
    },
    "00700002" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "GraphicLayer"
    },
    "00700003" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BoundingBoxAnnotationUnits"
    },
    "00700004" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AnchorPointAnnotationUnits"
    },
    "00700005" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "GraphicAnnotationUnits"
    },
    "00700006" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "UnformattedTextValue"
    },
    "00700008" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TextObjectSequence"
    },
    "00700009" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "GraphicObjectSequence"
    },
    "00700010" : {
        "VR" : "FL",
        "VM" : "2",
        "KEYWORD" : "BoundingBoxTopLeftHandCorner"
    },
    "00700011" : {
        "VR" : "FL",
        "VM" : "2",
        "KEYWORD" : "BoundingBoxBottomRightHandCorner"
    },
    "00700012" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BoundingBoxTextHorizontalJustification"
    },
    "00700014" : {
        "VR" : "FL",
        "VM" : "2",
        "KEYWORD" : "AnchorPoint"
    },
    "00700015" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AnchorPointVisibility"
    },
    "00700020" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "GraphicDimensions"
    },
    "00700021" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfGraphicPoints"
    },
    "00700022" : {
        "VR" : "FL",
        "VM" : "2-n",
        "KEYWORD" : "GraphicData"
    },
    "00700023" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "GraphicType"
    },
    "00700024" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "GraphicFilled"
    },
    "00700040" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ImageRotationRetired"
    },
    "00700041" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ImageHorizontalFlip"
    },
    "00700042" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ImageRotation"
    },
    "00700050" : {
        "VR" : "US",
        "VM" : "2",
        "KEYWORD" : "DisplayedAreaTopLeftHandCornerTrial"
    },
    "00700051" : {
        "VR" : "US",
        "VM" : "2",
        "KEYWORD" : "DisplayedAreaBottomRightHandCornerTrial"
    },
    "00700052" : {
        "VR" : "SL",
        "VM" : "2",
        "KEYWORD" : "DisplayedAreaTopLeftHandCorner"
    },
    "00700053" : {
        "VR" : "SL",
        "VM" : "2",
        "KEYWORD" : "DisplayedAreaBottomRightHandCorner"
    },
    "0070005A" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DisplayedAreaSelectionSequence"
    },
    "00700060" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "GraphicLayerSequence"
    },
    "00700062" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "GraphicLayerOrder"
    },
    "00700066" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "GraphicLayerRecommendedDisplayGrayscaleValue"
    },
    "00700067" : {
        "VR" : "US",
        "VM" : "3",
        "KEYWORD" : "GraphicLayerRecommendedDisplayRGBValue"
    },
    "00700068" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "GraphicLayerDescription"
    },
    "00700080" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ContentLabel"
    },
    "00700081" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ContentDescription"
    },
    "00700082" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "PresentationCreationDate"
    },
    "00700083" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "PresentationCreationTime"
    },
    "00700084" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "ContentCreatorName"
    },
    "00700086" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ContentCreatorIdentificationCodeSequence"
    },
    "00700087" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AlternateContentDescriptionSequence"
    },
    "00700100" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PresentationSizeMode"
    },
    "00700101" : {
        "VR" : "DS",
        "VM" : "2",
        "KEYWORD" : "PresentationPixelSpacing"
    },
    "00700102" : {
        "VR" : "IS",
        "VM" : "2",
        "KEYWORD" : "PresentationPixelAspectRatio"
    },
    "00700103" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "PresentationPixelMagnificationRatio"
    },
    "00700207" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "GraphicGroupLabel"
    },
    "00700208" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "GraphicGroupDescription"
    },
    "00700209" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CompoundGraphicSequence"
    },
    "00700226" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "CompoundGraphicInstanceID"
    },
    "00700227" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "FontName"
    },
    "00700228" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FontNameType"
    },
    "00700229" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "CSSFontName"
    },
    "00700230" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "RotationAngle"
    },
    "00700231" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TextStyleSequence"
    },
    "00700232" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "LineStyleSequence"
    },
    "00700233" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FillStyleSequence"
    },
    "00700234" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "GraphicGroupSequence"
    },
    "00700241" : {
        "VR" : "US",
        "VM" : "3",
        "KEYWORD" : "TextColorCIELabValue"
    },
    "00700242" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "HorizontalAlignment"
    },
    "00700243" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "VerticalAlignment"
    },
    "00700244" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ShadowStyle"
    },
    "00700245" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "ShadowOffsetX"
    },
    "00700246" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "ShadowOffsetY"
    },
    "00700247" : {
        "VR" : "US",
        "VM" : "3",
        "KEYWORD" : "ShadowColorCIELabValue"
    },
    "00700248" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "Underlined"
    },
    "00700249" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "Bold"
    },
    "00700250" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "Italic"
    },
    "00700251" : {
        "VR" : "US",
        "VM" : "3",
        "KEYWORD" : "PatternOnColorCIELabValue"
    },
    "00700252" : {
        "VR" : "US",
        "VM" : "3",
        "KEYWORD" : "PatternOffColorCIELabValue"
    },
    "00700253" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "LineThickness"
    },
    "00700254" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "LineDashingStyle"
    },
    "00700255" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "LinePattern"
    },
    "00700256" : {
        "VR" : "OB",
        "VM" : "1",
        "KEYWORD" : "FillPattern"
    },
    "00700257" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FillMode"
    },
    "00700258" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "ShadowOpacity"
    },
    "00700261" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "GapLength"
    },
    "00700262" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "DiameterOfVisibility"
    },
    "00700273" : {
        "VR" : "FL",
        "VM" : "2",
        "KEYWORD" : "RotationPoint"
    },
    "00700274" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TickAlignment"
    },
    "00700278" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ShowTickLabel"
    },
    "00700279" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TickLabelAlignment"
    },
    "00700282" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CompoundGraphicUnits"
    },
    "00700284" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "PatternOnOpacity"
    },
    "00700285" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "PatternOffOpacity"
    },
    "00700287" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MajorTicksSequence"
    },
    "00700288" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "TickPosition"
    },
    "00700289" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "TickLabel"
    },
    "00700294" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CompoundGraphicType"
    },
    "00700295" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "GraphicGroupID"
    },
    "00700306" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ShapeType"
    },
    "00700308" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RegistrationSequence"
    },
    "00700309" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MatrixRegistrationSequence"
    },
    "0070030A" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MatrixSequence"
    },
    "0070030C" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FrameOfReferenceTransformationMatrixType"
    },
    "0070030D" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RegistrationTypeCodeSequence"
    },
    "0070030F" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "FiducialDescription"
    },
    "00700310" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "FiducialIdentifier"
    },
    "00700311" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FiducialIdentifierCodeSequence"
    },
    "00700312" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ContourUncertaintyRadius"
    },
    "00700314" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "UsedFiducialsSequence"
    },
    "00700318" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "GraphicCoordinatesDataSequence"
    },
    "0070031A" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "FiducialUID"
    },
    "0070031C" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FiducialSetSequence"
    },
    "0070031E" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FiducialSequence"
    },
    "00700401" : {
        "VR" : "US",
        "VM" : "3",
        "KEYWORD" : "GraphicLayerRecommendedDisplayCIELabValue"
    },
    "00700402" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BlendingSequence"
    },
    "00700403" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "RelativeOpacity"
    },
    "00700404" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedSpatialRegistrationSequence"
    },
    "00700405" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BlendingPosition"
    },
    "00720002" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "HangingProtocolName"
    },
    "00720004" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "HangingProtocolDescription"
    },
    "00720006" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "HangingProtocolLevel"
    },
    "00720008" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "HangingProtocolCreator"
    },
    "0072000A" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "HangingProtocolCreationDateTime"
    },
    "0072000C" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "HangingProtocolDefinitionSequence"
    },
    "0072000E" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "HangingProtocolUserIdentificationCodeSequence"
    },
    "00720010" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "HangingProtocolUserGroupName"
    },
    "00720012" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SourceHangingProtocolSequence"
    },
    "00720014" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfPriorsReferenced"
    },
    "00720020" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ImageSetsSequence"
    },
    "00720022" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ImageSetSelectorSequence"
    },
    "00720024" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ImageSetSelectorUsageFlag"
    },
    "00720026" : {
        "VR" : "AT",
        "VM" : "1",
        "KEYWORD" : "SelectorAttribute"
    },
    "00720028" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "SelectorValueNumber"
    },
    "00720030" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TimeBasedImageSetsSequence"
    },
    "00720032" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ImageSetNumber"
    },
    "00720034" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ImageSetSelectorCategory"
    },
    "00720038" : {
        "VR" : "US",
        "VM" : "2",
        "KEYWORD" : "RelativeTime"
    },
    "0072003A" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RelativeTimeUnits"
    },
    "0072003C" : {
        "VR" : "SS",
        "VM" : "2",
        "KEYWORD" : "AbstractPriorValue"
    },
    "0072003E" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AbstractPriorCodeSequence"
    },
    "00720040" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ImageSetLabel"
    },
    "00720050" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SelectorAttributeVR"
    },
    "00720052" : {
        "VR" : "AT",
        "VM" : "1-n",
        "KEYWORD" : "SelectorSequencePointer"
    },
    "00720054" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "SelectorSequencePointerPrivateCreator"
    },
    "00720056" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SelectorAttributePrivateCreator"
    },
    "00720060" : {
        "VR" : "AT",
        "VM" : "1-n",
        "KEYWORD" : "SelectorATValue"
    },
    "00720062" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "SelectorCSValue"
    },
    "00720064" : {
        "VR" : "IS",
        "VM" : "1-n",
        "KEYWORD" : "SelectorISValue"
    },
    "00720066" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "SelectorLOValue"
    },
    "00720068" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "SelectorLTValue"
    },
    "0072006A" : {
        "VR" : "PN",
        "VM" : "1-n",
        "KEYWORD" : "SelectorPNValue"
    },
    "0072006C" : {
        "VR" : "SH",
        "VM" : "1-n",
        "KEYWORD" : "SelectorSHValue"
    },
    "0072006E" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "SelectorSTValue"
    },
    "00720070" : {
        "VR" : "UT",
        "VM" : "1",
        "KEYWORD" : "SelectorUTValue"
    },
    "00720072" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "SelectorDSValue"
    },
    "00720074" : {
        "VR" : "FD",
        "VM" : "1-n",
        "KEYWORD" : "SelectorFDValue"
    },
    "00720076" : {
        "VR" : "FL",
        "VM" : "1-n",
        "KEYWORD" : "SelectorFLValue"
    },
    "00720078" : {
        "VR" : "UL",
        "VM" : "1-n",
        "KEYWORD" : "SelectorULValue"
    },
    "0072007A" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "SelectorUSValue"
    },
    "0072007C" : {
        "VR" : "SL",
        "VM" : "1-n",
        "KEYWORD" : "SelectorSLValue"
    },
    "0072007E" : {
        "VR" : "SS",
        "VM" : "1-n",
        "KEYWORD" : "SelectorSSValue"
    },
    "00720080" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SelectorCodeSequenceValue"
    },
    "00720100" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfScreens"
    },
    "00720102" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "NominalScreenDefinitionSequence"
    },
    "00720104" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfVerticalPixels"
    },
    "00720106" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfHorizontalPixels"
    },
    "00720108" : {
        "VR" : "FD",
        "VM" : "4",
        "KEYWORD" : "DisplayEnvironmentSpatialPosition"
    },
    "0072010A" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ScreenMinimumGrayscaleBitDepth"
    },
    "0072010C" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ScreenMinimumColorBitDepth"
    },
    "0072010E" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ApplicationMaximumRepaintTime"
    },
    "00720200" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DisplaySetsSequence"
    },
    "00720202" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "DisplaySetNumber"
    },
    "00720203" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "DisplaySetLabel"
    },
    "00720204" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "DisplaySetPresentationGroup"
    },
    "00720206" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "DisplaySetPresentationGroupDescription"
    },
    "00720208" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PartialDataDisplayHandling"
    },
    "00720210" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SynchronizedScrollingSequence"
    },
    "00720212" : {
        "VR" : "US",
        "VM" : "2-n",
        "KEYWORD" : "DisplaySetScrollingGroup"
    },
    "00720214" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "NavigationIndicatorSequence"
    },
    "00720216" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NavigationDisplaySet"
    },
    "00720218" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "ReferenceDisplaySets"
    },
    "00720300" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ImageBoxesSequence"
    },
    "00720302" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ImageBoxNumber"
    },
    "00720304" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ImageBoxLayoutType"
    },
    "00720306" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ImageBoxTileHorizontalDimension"
    },
    "00720308" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ImageBoxTileVerticalDimension"
    },
    "00720310" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ImageBoxScrollDirection"
    },
    "00720312" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ImageBoxSmallScrollType"
    },
    "00720314" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ImageBoxSmallScrollAmount"
    },
    "00720316" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ImageBoxLargeScrollType"
    },
    "00720318" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ImageBoxLargeScrollAmount"
    },
    "00720320" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ImageBoxOverlapPriority"
    },
    "00720330" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "CineRelativeToRealTime"
    },
    "00720400" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FilterOperationsSequence"
    },
    "00720402" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FilterByCategory"
    },
    "00720404" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FilterByAttributePresence"
    },
    "00720406" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FilterByOperator"
    },
    "00720420" : {
        "VR" : "US",
        "VM" : "3",
        "KEYWORD" : "StructuredDisplayBackgroundCIELabValue"
    },
    "00720421" : {
        "VR" : "US",
        "VM" : "3",
        "KEYWORD" : "EmptyImageBoxCIELabValue"
    },
    "00720422" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "StructuredDisplayImageBoxSequence"
    },
    "00720424" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "StructuredDisplayTextBoxSequence"
    },
    "00720427" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedFirstFrameSequence"
    },
    "00720430" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ImageBoxSynchronizationSequence"
    },
    "00720432" : {
        "VR" : "US",
        "VM" : "2-n",
        "KEYWORD" : "SynchronizedImageBoxList"
    },
    "00720434" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TypeOfSynchronization"
    },
    "00720500" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BlendingOperationType"
    },
    "00720510" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ReformattingOperationType"
    },
    "00720512" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ReformattingThickness"
    },
    "00720514" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ReformattingInterval"
    },
    "00720516" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ReformattingOperationInitialViewDirection"
    },
    "00720520" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "ThreeDRenderingType"
    },
    "00720600" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SortingOperationsSequence"
    },
    "00720602" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SortByCategory"
    },
    "00720604" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SortingDirection"
    },
    "00720700" : {
        "VR" : "CS",
        "VM" : "2",
        "KEYWORD" : "DisplaySetPatientOrientation"
    },
    "00720702" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "VOIType"
    },
    "00720704" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PseudoColorType"
    },
    "00720705" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PseudoColorPaletteInstanceReferenceSequence"
    },
    "00720706" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ShowGrayscaleInverted"
    },
    "00720710" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ShowImageTrueSizeFlag"
    },
    "00720712" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ShowGraphicAnnotationFlag"
    },
    "00720714" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ShowPatientDemographicsFlag"
    },
    "00720716" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ShowAcquisitionTechniquesFlag"
    },
    "00720717" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DisplaySetHorizontalJustification"
    },
    "00720718" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DisplaySetVerticalJustification"
    },
    "00740120" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ContinuationStartMeterset"
    },
    "00740121" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "ContinuationEndMeterset"
    },
    "00741000" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ProcedureStepState"
    },
    "00741002" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ProcedureStepProgressInformationSequence"
    },
    "00741004" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ProcedureStepProgress"
    },
    "00741006" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "ProcedureStepProgressDescription"
    },
    "00741008" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ProcedureStepCommunicationsURISequence"
    },
    "0074100a" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "ContactURI"
    },
    "0074100c" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ContactDisplayName"
    },
    "0074100e" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ProcedureStepDiscontinuationReasonCodeSequence"
    },
    "00741020" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BeamTaskSequence"
    },
    "00741022" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BeamTaskType"
    },
    "00741024" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "BeamOrderIndexTrial"
    },
    "00741026" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TableTopVerticalAdjustedPosition"
    },
    "00741027" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TableTopLongitudinalAdjustedPosition"
    },
    "00741028" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TableTopLateralAdjustedPosition"
    },
    "0074102A" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "PatientSupportAdjustedAngle"
    },
    "0074102B" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TableTopEccentricAdjustedAngle"
    },
    "0074102C" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TableTopPitchAdjustedAngle"
    },
    "0074102D" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "TableTopRollAdjustedAngle"
    },
    "00741030" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DeliveryVerificationImageSequence"
    },
    "00741032" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "VerificationImageTiming"
    },
    "00741034" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DoubleExposureFlag"
    },
    "00741036" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DoubleExposureOrdering"
    },
    "00741038" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DoubleExposureMetersetTrial"
    },
    "0074103A" : {
        "VR" : "DS",
        "VM" : "4",
        "KEYWORD" : "DoubleExposureFieldDeltaTrial"
    },
    "00741040" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RelatedReferenceRTImageSequence"
    },
    "00741042" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "GeneralMachineVerificationSequence"
    },
    "00741044" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ConventionalMachineVerificationSequence"
    },
    "00741046" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IonMachineVerificationSequence"
    },
    "00741048" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FailedAttributesSequence"
    },
    "0074104A" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OverriddenAttributesSequence"
    },
    "0074104C" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ConventionalControlPointVerificationSequence"
    },
    "0074104E" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IonControlPointVerificationSequence"
    },
    "00741050" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AttributeOccurrenceSequence"
    },
    "00741052" : {
        "VR" : "AT",
        "VM" : "1",
        "KEYWORD" : "AttributeOccurrencePointer"
    },
    "00741054" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "AttributeItemSelector"
    },
    "00741056" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "AttributeOccurrencePrivateCreator"
    },
    "00741057" : {
        "VR" : "IS",
        "VM" : "1-n",
        "KEYWORD" : "SelectorSequencePointerItems"
    },
    "00741200" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ScheduledProcedureStepPriority"
    },
    "00741202" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "WorklistLabel"
    },
    "00741204" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ProcedureStepLabel"
    },
    "00741210" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ScheduledProcessingParametersSequence"
    },
    "00741212" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PerformedProcessingParametersSequence"
    },
    "00741216" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "UnifiedProcedureStepPerformedProcedureSequence"
    },
    "00741220" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RelatedProcedureStepSequence"
    },
    "00741222" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ProcedureStepRelationshipType"
    },
    "00741224" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReplacedProcedureStepSequence"
    },
    "00741230" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "DeletionLock"
    },
    "00741234" : {
        "VR" : "AE",
        "VM" : "1",
        "KEYWORD" : "ReceivingAE"
    },
    "00741236" : {
        "VR" : "AE",
        "VM" : "1",
        "KEYWORD" : "RequestingAE"
    },
    "00741238" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "ReasonForCancellation"
    },
    "00741242" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SCPStatus"
    },
    "00741244" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SubscriptionListStatus"
    },
    "00741246" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "UnifiedProcedureStepListStatus"
    },
    "00741324" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "BeamOrderIndex"
    },
    "00741338" : {
        "VR" : "FD",
        "VM" : "1",
        "KEYWORD" : "DoubleExposureMeterset"
    },
    "0074133A" : {
        "VR" : "FD",
        "VM" : "4",
        "KEYWORD" : "DoubleExposureFieldDelta"
    },
    "00760001" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ImplantAssemblyTemplateName"
    },
    "00760003" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ImplantAssemblyTemplateIssuer"
    },
    "00760006" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ImplantAssemblyTemplateVersion"
    },
    "00760008" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReplacedImplantAssemblyTemplateSequence"
    },
    "0076000A" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ImplantAssemblyTemplateType"
    },
    "0076000C" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OriginalImplantAssemblyTemplateSequence"
    },
    "0076000E" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DerivationImplantAssemblyTemplateSequence"
    },
    "00760010" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ImplantAssemblyTemplateTargetAnatomySequence"
    },
    "00760020" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ProcedureTypeCodeSequence"
    },
    "00760030" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SurgicalTechnique"
    },
    "00760032" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ComponentTypesSequence"
    },
    "00760034" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ComponentTypeCodeSequence"
    },
    "00760036" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ExclusiveComponentType"
    },
    "00760038" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "MandatoryComponentType"
    },
    "00760040" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ComponentSequence"
    },
    "00760055" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ComponentID"
    },
    "00760060" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ComponentAssemblySequence"
    },
    "00760070" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "Component1ReferencedID"
    },
    "00760080" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "Component1ReferencedMatingFeatureSetID"
    },
    "00760090" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "Component1ReferencedMatingFeatureID"
    },
    "007600A0" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "Component2ReferencedID"
    },
    "007600B0" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "Component2ReferencedMatingFeatureSetID"
    },
    "007600C0" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "Component2ReferencedMatingFeatureID"
    },
    "00780001" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ImplantTemplateGroupName"
    },
    "00780010" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "ImplantTemplateGroupDescription"
    },
    "00780020" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ImplantTemplateGroupIssuer"
    },
    "00780024" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ImplantTemplateGroupVersion"
    },
    "00780026" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReplacedImplantTemplateGroupSequence"
    },
    "00780028" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ImplantTemplateGroupTargetAnatomySequence"
    },
    "0078002A" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ImplantTemplateGroupMembersSequence"
    },
    "0078002E" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ImplantTemplateGroupMemberID"
    },
    "00780050" : {
        "VR" : "FD",
        "VM" : "3",
        "KEYWORD" : "ThreeDImplantTemplateGroupMemberMatchingPoint"
    },
    "00780060" : {
        "VR" : "FD",
        "VM" : "9",
        "KEYWORD" : "ThreeDImplantTemplateGroupMemberMatchingAxes"
    },
    "00780070" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ImplantTemplateGroupMemberMatching2DCoordinatesSequence"
    },
    "00780090" : {
        "VR" : "FD",
        "VM" : "2",
        "KEYWORD" : "TwoDImplantTemplateGroupMemberMatchingPoint"
    },
    "007800A0" : {
        "VR" : "FD",
        "VM" : "4",
        "KEYWORD" : "TwoDImplantTemplateGroupMemberMatchingAxes"
    },
    "007800B0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ImplantTemplateGroupVariationDimensionSequence"
    },
    "007800B2" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ImplantTemplateGroupVariationDimensionName"
    },
    "007800B4" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ImplantTemplateGroupVariationDimensionRankSequence"
    },
    "007800B6" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ReferencedImplantTemplateGroupMemberID"
    },
    "007800B8" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ImplantTemplateGroupVariationDimensionRank"
    },
    "00880130" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "StorageMediaFileSetID"
    },
    "00880140" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "StorageMediaFileSetUID"
    },
    "00880200" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IconImageSequence"
    },
    "00880904" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "TopicTitle"
    },
    "00880906" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "TopicSubject"
    },
    "00880910" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "TopicAuthor"
    },
    "00880912" : {
        "VR" : "LO",
        "VM" : "1-32",
        "KEYWORD" : "TopicKeywords"
    },
    "01000410" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SOPInstanceStatus"
    },
    "01000420" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "SOPAuthorizationDateTime"
    },
    "01000424" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "SOPAuthorizationComment"
    },
    "01000426" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "AuthorizationEquipmentCertificationNumber"
    },
    "04000005" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "MACIDNumber"
    },
    "04000010" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "MACCalculationTransferSyntaxUID"
    },
    "04000015" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "MACAlgorithm"
    },
    "04000020" : {
        "VR" : "AT",
        "VM" : "1-n",
        "KEYWORD" : "DataElementsSigned"
    },
    "04000100" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "DigitalSignatureUID"
    },
    "04000105" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "DigitalSignatureDateTime"
    },
    "04000110" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CertificateType"
    },
    "04000115" : {
        "VR" : "OB",
        "VM" : "1",
        "KEYWORD" : "CertificateOfSigner"
    },
    "04000120" : {
        "VR" : "OB",
        "VM" : "1",
        "KEYWORD" : "Signature"
    },
    "04000305" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CertifiedTimestampType"
    },
    "04000310" : {
        "VR" : "OB",
        "VM" : "1",
        "KEYWORD" : "CertifiedTimestamp"
    },
    "04000401" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DigitalSignaturePurposeCodeSequence"
    },
    "04000402" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedDigitalSignatureSequence"
    },
    "04000403" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedSOPInstanceMACSequence"
    },
    "04000404" : {
        "VR" : "OB",
        "VM" : "1",
        "KEYWORD" : "MAC"
    },
    "04000500" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "EncryptedAttributesSequence"
    },
    "04000510" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "EncryptedContentTransferSyntaxUID"
    },
    "04000520" : {
        "VR" : "OB",
        "VM" : "1",
        "KEYWORD" : "EncryptedContent"
    },
    "04000550" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ModifiedAttributesSequence"
    },
    "04000561" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OriginalAttributesSequence"
    },
    "04000562" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "AttributeModificationDateTime"
    },
    "04000563" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ModifyingSystem"
    },
    "04000564" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SourceOfPreviousValues"
    },
    "04000565" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ReasonForTheAttributeModification"
    },
    "1000xxx0" : {
        "VR" : "US",
        "VM" : "3",
        "KEYWORD" : "EscapeTriplet"
    },
    "1000xxx1" : {
        "VR" : "US",
        "VM" : "3",
        "KEYWORD" : "RunLengthTriplet"
    },
    "1000xxx2" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "HuffmanTableSize"
    },
    "1000xxx3" : {
        "VR" : "US",
        "VM" : "3",
        "KEYWORD" : "HuffmanTableTriplet"
    },
    "1000xxx4" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ShiftTableSize"
    },
    "1000xxx5" : {
        "VR" : "US",
        "VM" : "3",
        "KEYWORD" : "ShiftTableTriplet"
    },
    "1010xxxx" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "ZonalMap"
    },
    "20000010" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfCopies"
    },
    "2000001E" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PrinterConfigurationSequence"
    },
    "20000020" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PrintPriority"
    },
    "20000030" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "MediumType"
    },
    "20000040" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FilmDestination"
    },
    "20000050" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "FilmSessionLabel"
    },
    "20000060" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "MemoryAllocation"
    },
    "20000061" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "MaximumMemoryAllocation"
    },
    "20000062" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ColorImagePrintingFlag"
    },
    "20000063" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CollationFlag"
    },
    "20000065" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AnnotationFlag"
    },
    "20000067" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ImageOverlayFlag"
    },
    "20000069" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PresentationLUTFlag"
    },
    "2000006A" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ImageBoxPresentationLUTFlag"
    },
    "200000A0" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "MemoryBitDepth"
    },
    "200000A1" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "PrintingBitDepth"
    },
    "200000A2" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MediaInstalledSequence"
    },
    "200000A4" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OtherMediaAvailableSequence"
    },
    "200000A8" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SupportedImageDisplayFormatsSequence"
    },
    "20000500" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedFilmBoxSequence"
    },
    "20000510" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedStoredPrintSequence"
    },
    "20100010" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "ImageDisplayFormat"
    },
    "20100030" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AnnotationDisplayFormatID"
    },
    "20100040" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FilmOrientation"
    },
    "20100050" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FilmSizeID"
    },
    "20100052" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PrinterResolutionID"
    },
    "20100054" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DefaultPrinterResolutionID"
    },
    "20100060" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "MagnificationType"
    },
    "20100080" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SmoothingType"
    },
    "201000A6" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DefaultMagnificationType"
    },
    "201000A7" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "OtherMagnificationTypesAvailable"
    },
    "201000A8" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DefaultSmoothingType"
    },
    "201000A9" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "OtherSmoothingTypesAvailable"
    },
    "20100100" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BorderDensity"
    },
    "20100110" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "EmptyImageDensity"
    },
    "20100120" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "MinDensity"
    },
    "20100130" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "MaxDensity"
    },
    "20100140" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "Trim"
    },
    "20100150" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "ConfigurationInformation"
    },
    "20100152" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "ConfigurationInformationDescription"
    },
    "20100154" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "MaximumCollatedFilms"
    },
    "2010015E" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "Illumination"
    },
    "20100160" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ReflectedAmbientLight"
    },
    "20100376" : {
        "VR" : "DS",
        "VM" : "2",
        "KEYWORD" : "PrinterPixelSpacing"
    },
    "20100500" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedFilmSessionSequence"
    },
    "20100510" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedImageBoxSequence"
    },
    "20100520" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedBasicAnnotationBoxSequence"
    },
    "20200010" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ImageBoxPosition"
    },
    "20200020" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "Polarity"
    },
    "20200030" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "RequestedImageSize"
    },
    "20200040" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RequestedDecimateCropBehavior"
    },
    "20200050" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RequestedResolutionID"
    },
    "202000A0" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RequestedImageSizeFlag"
    },
    "202000A2" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DecimateCropResult"
    },
    "20200110" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BasicGrayscaleImageSequence"
    },
    "20200111" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BasicColorImageSequence"
    },
    "20200130" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedImageOverlayBoxSequence"
    },
    "20200140" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedVOILUTBoxSequence"
    },
    "20300010" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "AnnotationPosition"
    },
    "20300020" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "TextString"
    },
    "20400010" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedOverlayPlaneSequence"
    },
    "20400011" : {
        "VR" : "US",
        "VM" : "1-99",
        "KEYWORD" : "ReferencedOverlayPlaneGroups"
    },
    "20400020" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OverlayPixelDataSequence"
    },
    "20400060" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OverlayMagnificationType"
    },
    "20400070" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OverlaySmoothingType"
    },
    "20400072" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OverlayOrImageMagnification"
    },
    "20400074" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "MagnifyToNumberOfColumns"
    },
    "20400080" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OverlayForegroundDensity"
    },
    "20400082" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OverlayBackgroundDensity"
    },
    "20400090" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OverlayMode"
    },
    "20400100" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ThresholdDensity"
    },
    "20400500" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedImageBoxSequenceRetired"
    },
    "20500010" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PresentationLUTSequence"
    },
    "20500020" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PresentationLUTShape"
    },
    "20500500" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedPresentationLUTSequence"
    },
    "21000010" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "PrintJobID"
    },
    "21000020" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ExecutionStatus"
    },
    "21000030" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ExecutionStatusInfo"
    },
    "21000040" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "CreationDate"
    },
    "21000050" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "CreationTime"
    },
    "21000070" : {
        "VR" : "AE",
        "VM" : "1",
        "KEYWORD" : "Originator"
    },
    "21000140" : {
        "VR" : "AE",
        "VM" : "1",
        "KEYWORD" : "DestinationAE"
    },
    "21000160" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "OwnerID"
    },
    "21000170" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfFilms"
    },
    "21000500" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedPrintJobSequencePullStoredPrint"
    },
    "21100010" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PrinterStatus"
    },
    "21100020" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PrinterStatusInfo"
    },
    "21100030" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PrinterName"
    },
    "21100099" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "PrintQueueID"
    },
    "21200010" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "QueueStatus"
    },
    "21200050" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PrintJobDescriptionSequence"
    },
    "21200070" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedPrintJobSequence"
    },
    "21300010" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PrintManagementCapabilitiesSequence"
    },
    "21300015" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PrinterCharacteristicsSequence"
    },
    "21300030" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FilmBoxContentSequence"
    },
    "21300040" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ImageBoxContentSequence"
    },
    "21300050" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AnnotationContentSequence"
    },
    "21300060" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ImageOverlayBoxContentSequence"
    },
    "21300080" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PresentationLUTContentSequence"
    },
    "213000A0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ProposedStudySequence"
    },
    "213000C0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OriginalImageSequence"
    },
    "22000001" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "LabelUsingInformationExtractedFromInstances"
    },
    "22000002" : {
        "VR" : "UT",
        "VM" : "1",
        "KEYWORD" : "LabelText"
    },
    "22000003" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "LabelStyleSelection"
    },
    "22000004" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "MediaDisposition"
    },
    "22000005" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "BarcodeValue"
    },
    "22000006" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BarcodeSymbology"
    },
    "22000007" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AllowMediaSplitting"
    },
    "22000008" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "IncludeNonDICOMObjects"
    },
    "22000009" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "IncludeDisplayApplication"
    },
    "2200000A" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PreserveCompositeInstancesAfterMediaCreation"
    },
    "2200000B" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "TotalNumberOfPiecesOfMediaCreated"
    },
    "2200000C" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "RequestedMediaApplicationProfile"
    },
    "2200000D" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedStorageMediaSequence"
    },
    "2200000E" : {
        "VR" : "AT",
        "VM" : "1-n",
        "KEYWORD" : "FailureAttributes"
    },
    "2200000F" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AllowLossyCompression"
    },
    "22000020" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RequestPriority"
    },
    "30020002" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "RTImageLabel"
    },
    "30020003" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "RTImageName"
    },
    "30020004" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "RTImageDescription"
    },
    "3002000A" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ReportedValuesOrigin"
    },
    "3002000C" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RTImagePlane"
    },
    "3002000D" : {
        "VR" : "DS",
        "VM" : "3",
        "KEYWORD" : "XRayImageReceptorTranslation"
    },
    "3002000E" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "XRayImageReceptorAngle"
    },
    "30020010" : {
        "VR" : "DS",
        "VM" : "6",
        "KEYWORD" : "RTImageOrientation"
    },
    "30020011" : {
        "VR" : "DS",
        "VM" : "2",
        "KEYWORD" : "ImagePlanePixelSpacing"
    },
    "30020012" : {
        "VR" : "DS",
        "VM" : "2",
        "KEYWORD" : "RTImagePosition"
    },
    "30020020" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "RadiationMachineName"
    },
    "30020022" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "RadiationMachineSAD"
    },
    "30020024" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "RadiationMachineSSD"
    },
    "30020026" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "RTImageSID"
    },
    "30020028" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SourceToReferenceObjectDistance"
    },
    "30020029" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "FractionNumber"
    },
    "30020030" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ExposureSequence"
    },
    "30020032" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "MetersetExposure"
    },
    "30020034" : {
        "VR" : "DS",
        "VM" : "4",
        "KEYWORD" : "DiaphragmPosition"
    },
    "30020040" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FluenceMapSequence"
    },
    "30020041" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FluenceDataSource"
    },
    "30020042" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "FluenceDataScale"
    },
    "30020050" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PrimaryFluenceModeSequence"
    },
    "30020051" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FluenceMode"
    },
    "30020052" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "FluenceModeID"
    },
    "30040001" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DVHType"
    },
    "30040002" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DoseUnits"
    },
    "30040004" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DoseType"
    },
    "30040006" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "DoseComment"
    },
    "30040008" : {
        "VR" : "DS",
        "VM" : "3",
        "KEYWORD" : "NormalizationPoint"
    },
    "3004000A" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DoseSummationType"
    },
    "3004000C" : {
        "VR" : "DS",
        "VM" : "2-n",
        "KEYWORD" : "GridFrameOffsetVector"
    },
    "3004000E" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DoseGridScaling"
    },
    "30040010" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RTDoseROISequence"
    },
    "30040012" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DoseValue"
    },
    "30040014" : {
        "VR" : "CS",
        "VM" : "1-3",
        "KEYWORD" : "TissueHeterogeneityCorrection"
    },
    "30040040" : {
        "VR" : "DS",
        "VM" : "3",
        "KEYWORD" : "DVHNormalizationPoint"
    },
    "30040042" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DVHNormalizationDoseValue"
    },
    "30040050" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DVHSequence"
    },
    "30040052" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DVHDoseScaling"
    },
    "30040054" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DVHVolumeUnits"
    },
    "30040056" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "DVHNumberOfBins"
    },
    "30040058" : {
        "VR" : "DS",
        "VM" : "2-2n",
        "KEYWORD" : "DVHData"
    },
    "30040060" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DVHReferencedROISequence"
    },
    "30040062" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DVHROIContributionType"
    },
    "30040070" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DVHMinimumDose"
    },
    "30040072" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DVHMaximumDose"
    },
    "30040074" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DVHMeanDose"
    },
    "30060002" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "StructureSetLabel"
    },
    "30060004" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "StructureSetName"
    },
    "30060006" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "StructureSetDescription"
    },
    "30060008" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "StructureSetDate"
    },
    "30060009" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "StructureSetTime"
    },
    "30060010" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedFrameOfReferenceSequence"
    },
    "30060012" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RTReferencedStudySequence"
    },
    "30060014" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RTReferencedSeriesSequence"
    },
    "30060016" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ContourImageSequence"
    },
    "30060020" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "StructureSetROISequence"
    },
    "30060022" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ROINumber"
    },
    "30060024" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "ReferencedFrameOfReferenceUID"
    },
    "30060026" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ROIName"
    },
    "30060028" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "ROIDescription"
    },
    "3006002A" : {
        "VR" : "IS",
        "VM" : "3",
        "KEYWORD" : "ROIDisplayColor"
    },
    "3006002C" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ROIVolume"
    },
    "30060030" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RTRelatedROISequence"
    },
    "30060033" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RTROIRelationship"
    },
    "30060036" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ROIGenerationAlgorithm"
    },
    "30060038" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ROIGenerationDescription"
    },
    "30060039" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ROIContourSequence"
    },
    "30060040" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ContourSequence"
    },
    "30060042" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ContourGeometricType"
    },
    "30060044" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ContourSlabThickness"
    },
    "30060045" : {
        "VR" : "DS",
        "VM" : "3",
        "KEYWORD" : "ContourOffsetVector"
    },
    "30060046" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfContourPoints"
    },
    "30060048" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ContourNumber"
    },
    "30060049" : {
        "VR" : "IS",
        "VM" : "1-n",
        "KEYWORD" : "AttachedContours"
    },
    "30060050" : {
        "VR" : "DS",
        "VM" : "3-3n",
        "KEYWORD" : "ContourData"
    },
    "30060080" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RTROIObservationsSequence"
    },
    "30060082" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ObservationNumber"
    },
    "30060084" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedROINumber"
    },
    "30060085" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "ROIObservationLabel"
    },
    "30060086" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RTROIIdentificationCodeSequence"
    },
    "30060088" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "ROIObservationDescription"
    },
    "300600A0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RelatedRTROIObservationsSequence"
    },
    "300600A4" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RTROIInterpretedType"
    },
    "300600A6" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "ROIInterpreter"
    },
    "300600B0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ROIPhysicalPropertiesSequence"
    },
    "300600B2" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ROIPhysicalProperty"
    },
    "300600B4" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ROIPhysicalPropertyValue"
    },
    "300600B6" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ROIElementalCompositionSequence"
    },
    "300600B7" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ROIElementalCompositionAtomicNumber"
    },
    "300600B8" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "ROIElementalCompositionAtomicMassFraction"
    },
    "300600C0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FrameOfReferenceRelationshipSequence"
    },
    "300600C2" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "RelatedFrameOfReferenceUID"
    },
    "300600C4" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FrameOfReferenceTransformationType"
    },
    "300600C6" : {
        "VR" : "DS",
        "VM" : "16",
        "KEYWORD" : "FrameOfReferenceTransformationMatrix"
    },
    "300600C8" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "FrameOfReferenceTransformationComment"
    },
    "30080010" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MeasuredDoseReferenceSequence"
    },
    "30080012" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "MeasuredDoseDescription"
    },
    "30080014" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "MeasuredDoseType"
    },
    "30080016" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "MeasuredDoseValue"
    },
    "30080020" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TreatmentSessionBeamSequence"
    },
    "30080021" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TreatmentSessionIonBeamSequence"
    },
    "30080022" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "CurrentFractionNumber"
    },
    "30080024" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "TreatmentControlPointDate"
    },
    "30080025" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "TreatmentControlPointTime"
    },
    "3008002A" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TreatmentTerminationStatus"
    },
    "3008002B" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "TreatmentTerminationCode"
    },
    "3008002C" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TreatmentVerificationStatus"
    },
    "30080030" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedTreatmentRecordSequence"
    },
    "30080032" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SpecifiedPrimaryMeterset"
    },
    "30080033" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SpecifiedSecondaryMeterset"
    },
    "30080036" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DeliveredPrimaryMeterset"
    },
    "30080037" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DeliveredSecondaryMeterset"
    },
    "3008003A" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SpecifiedTreatmentTime"
    },
    "3008003B" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DeliveredTreatmentTime"
    },
    "30080040" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ControlPointDeliverySequence"
    },
    "30080041" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IonControlPointDeliverySequence"
    },
    "30080042" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SpecifiedMeterset"
    },
    "30080044" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DeliveredMeterset"
    },
    "30080045" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "MetersetRateSet"
    },
    "30080046" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "MetersetRateDelivered"
    },
    "30080047" : {
        "VR" : "FL",
        "VM" : "1-n",
        "KEYWORD" : "ScanSpotMetersetsDelivered"
    },
    "30080048" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DoseRateDelivered"
    },
    "30080050" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TreatmentSummaryCalculatedDoseReferenceSequence"
    },
    "30080052" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "CumulativeDoseToDoseReference"
    },
    "30080054" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "FirstTreatmentDate"
    },
    "30080056" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "MostRecentTreatmentDate"
    },
    "3008005A" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfFractionsDelivered"
    },
    "30080060" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OverrideSequence"
    },
    "30080061" : {
        "VR" : "AT",
        "VM" : "1",
        "KEYWORD" : "ParameterSequencePointer"
    },
    "30080062" : {
        "VR" : "AT",
        "VM" : "1",
        "KEYWORD" : "OverrideParameterPointer"
    },
    "30080063" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ParameterItemIndex"
    },
    "30080064" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "MeasuredDoseReferenceNumber"
    },
    "30080065" : {
        "VR" : "AT",
        "VM" : "1",
        "KEYWORD" : "ParameterPointer"
    },
    "30080066" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "OverrideReason"
    },
    "30080068" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CorrectedParameterSequence"
    },
    "3008006A" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "CorrectionValue"
    },
    "30080070" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CalculatedDoseReferenceSequence"
    },
    "30080072" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "CalculatedDoseReferenceNumber"
    },
    "30080074" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "CalculatedDoseReferenceDescription"
    },
    "30080076" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "CalculatedDoseReferenceDoseValue"
    },
    "30080078" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "StartMeterset"
    },
    "3008007A" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "EndMeterset"
    },
    "30080080" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedMeasuredDoseReferenceSequence"
    },
    "30080082" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedMeasuredDoseReferenceNumber"
    },
    "30080090" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedCalculatedDoseReferenceSequence"
    },
    "30080092" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedCalculatedDoseReferenceNumber"
    },
    "300800A0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BeamLimitingDeviceLeafPairsSequence"
    },
    "300800B0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RecordedWedgeSequence"
    },
    "300800C0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RecordedCompensatorSequence"
    },
    "300800D0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RecordedBlockSequence"
    },
    "300800E0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TreatmentSummaryMeasuredDoseReferenceSequence"
    },
    "300800F0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RecordedSnoutSequence"
    },
    "300800F2" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RecordedRangeShifterSequence"
    },
    "300800F4" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RecordedLateralSpreadingDeviceSequence"
    },
    "300800F6" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RecordedRangeModulatorSequence"
    },
    "30080100" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RecordedSourceSequence"
    },
    "30080105" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SourceSerialNumber"
    },
    "30080110" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TreatmentSessionApplicationSetupSequence"
    },
    "30080116" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ApplicationSetupCheck"
    },
    "30080120" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RecordedBrachyAccessoryDeviceSequence"
    },
    "30080122" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedBrachyAccessoryDeviceNumber"
    },
    "30080130" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RecordedChannelSequence"
    },
    "30080132" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SpecifiedChannelTotalTime"
    },
    "30080134" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DeliveredChannelTotalTime"
    },
    "30080136" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "SpecifiedNumberOfPulses"
    },
    "30080138" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "DeliveredNumberOfPulses"
    },
    "3008013A" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SpecifiedPulseRepetitionInterval"
    },
    "3008013C" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DeliveredPulseRepetitionInterval"
    },
    "30080140" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RecordedSourceApplicatorSequence"
    },
    "30080142" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedSourceApplicatorNumber"
    },
    "30080150" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RecordedChannelShieldSequence"
    },
    "30080152" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedChannelShieldNumber"
    },
    "30080160" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BrachyControlPointDeliveredSequence"
    },
    "30080162" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "SafePositionExitDate"
    },
    "30080164" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "SafePositionExitTime"
    },
    "30080166" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "SafePositionReturnDate"
    },
    "30080168" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "SafePositionReturnTime"
    },
    "30080200" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CurrentTreatmentStatus"
    },
    "30080202" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "TreatmentStatusComment"
    },
    "30080220" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FractionGroupSummarySequence"
    },
    "30080223" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedFractionNumber"
    },
    "30080224" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FractionGroupType"
    },
    "30080230" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BeamStopperPosition"
    },
    "30080240" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FractionStatusSummarySequence"
    },
    "30080250" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "TreatmentDate"
    },
    "30080251" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "TreatmentTime"
    },
    "300A0002" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "RTPlanLabel"
    },
    "300A0003" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "RTPlanName"
    },
    "300A0004" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "RTPlanDescription"
    },
    "300A0006" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "RTPlanDate"
    },
    "300A0007" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "RTPlanTime"
    },
    "300A0009" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "TreatmentProtocols"
    },
    "300A000A" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PlanIntent"
    },
    "300A000B" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "TreatmentSites"
    },
    "300A000C" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RTPlanGeometry"
    },
    "300A000E" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "PrescriptionDescription"
    },
    "300A0010" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DoseReferenceSequence"
    },
    "300A0012" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "DoseReferenceNumber"
    },
    "300A0013" : {
        "VR" : "UI",
        "VM" : "1",
        "KEYWORD" : "DoseReferenceUID"
    },
    "300A0014" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DoseReferenceStructureType"
    },
    "300A0015" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "NominalBeamEnergyUnit"
    },
    "300A0016" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "DoseReferenceDescription"
    },
    "300A0018" : {
        "VR" : "DS",
        "VM" : "3",
        "KEYWORD" : "DoseReferencePointCoordinates"
    },
    "300A001A" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "NominalPriorDose"
    },
    "300A0020" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DoseReferenceType"
    },
    "300A0021" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ConstraintWeight"
    },
    "300A0022" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DeliveryWarningDose"
    },
    "300A0023" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DeliveryMaximumDose"
    },
    "300A0025" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TargetMinimumDose"
    },
    "300A0026" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TargetPrescriptionDose"
    },
    "300A0027" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TargetMaximumDose"
    },
    "300A0028" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TargetUnderdoseVolumeFraction"
    },
    "300A002A" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "OrganAtRiskFullVolumeDose"
    },
    "300A002B" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "OrganAtRiskLimitDose"
    },
    "300A002C" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "OrganAtRiskMaximumDose"
    },
    "300A002D" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "OrganAtRiskOverdoseVolumeFraction"
    },
    "300A0040" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ToleranceTableSequence"
    },
    "300A0042" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ToleranceTableNumber"
    },
    "300A0043" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "ToleranceTableLabel"
    },
    "300A0044" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "GantryAngleTolerance"
    },
    "300A0046" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "BeamLimitingDeviceAngleTolerance"
    },
    "300A0048" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BeamLimitingDeviceToleranceSequence"
    },
    "300A004A" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "BeamLimitingDevicePositionTolerance"
    },
    "300A004B" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "SnoutPositionTolerance"
    },
    "300A004C" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "PatientSupportAngleTolerance"
    },
    "300A004E" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TableTopEccentricAngleTolerance"
    },
    "300A004F" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "TableTopPitchAngleTolerance"
    },
    "300A0050" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "TableTopRollAngleTolerance"
    },
    "300A0051" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TableTopVerticalPositionTolerance"
    },
    "300A0052" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TableTopLongitudinalPositionTolerance"
    },
    "300A0053" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TableTopLateralPositionTolerance"
    },
    "300A0055" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RTPlanRelationship"
    },
    "300A0070" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FractionGroupSequence"
    },
    "300A0071" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "FractionGroupNumber"
    },
    "300A0072" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "FractionGroupDescription"
    },
    "300A0078" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfFractionsPlanned"
    },
    "300A0079" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfFractionPatternDigitsPerDay"
    },
    "300A007A" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "RepeatFractionCycleLength"
    },
    "300A007B" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "FractionPattern"
    },
    "300A0080" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfBeams"
    },
    "300A0082" : {
        "VR" : "DS",
        "VM" : "3",
        "KEYWORD" : "BeamDoseSpecificationPoint"
    },
    "300A0084" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "BeamDose"
    },
    "300A0086" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "BeamMeterset"
    },
    "300A0088" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "BeamDosePointDepth"
    },
    "300A0089" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "BeamDosePointEquivalentDepth"
    },
    "300A008A" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "BeamDosePointSSD"
    },
    "300A00A0" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfBrachyApplicationSetups"
    },
    "300A00A2" : {
        "VR" : "DS",
        "VM" : "3",
        "KEYWORD" : "BrachyApplicationSetupDoseSpecificationPoint"
    },
    "300A00A4" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "BrachyApplicationSetupDose"
    },
    "300A00B0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BeamSequence"
    },
    "300A00B2" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "TreatmentMachineName"
    },
    "300A00B3" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PrimaryDosimeterUnit"
    },
    "300A00B4" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SourceAxisDistance"
    },
    "300A00B6" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BeamLimitingDeviceSequence"
    },
    "300A00B8" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RTBeamLimitingDeviceType"
    },
    "300A00BA" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SourceToBeamLimitingDeviceDistance"
    },
    "300A00BB" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "IsocenterToBeamLimitingDeviceDistance"
    },
    "300A00BC" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfLeafJawPairs"
    },
    "300A00BE" : {
        "VR" : "DS",
        "VM" : "3-n",
        "KEYWORD" : "LeafPositionBoundaries"
    },
    "300A00C0" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "BeamNumber"
    },
    "300A00C2" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "BeamName"
    },
    "300A00C3" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "BeamDescription"
    },
    "300A00C4" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BeamType"
    },
    "300A00C6" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RadiationType"
    },
    "300A00C7" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "HighDoseTechniqueType"
    },
    "300A00C8" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferenceImageNumber"
    },
    "300A00CA" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PlannedVerificationImageSequence"
    },
    "300A00CC" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "ImagingDeviceSpecificAcquisitionParameters"
    },
    "300A00CE" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TreatmentDeliveryType"
    },
    "300A00D0" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfWedges"
    },
    "300A00D1" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "WedgeSequence"
    },
    "300A00D2" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "WedgeNumber"
    },
    "300A00D3" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "WedgeType"
    },
    "300A00D4" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "WedgeID"
    },
    "300A00D5" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "WedgeAngle"
    },
    "300A00D6" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "WedgeFactor"
    },
    "300A00D7" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "TotalWedgeTrayWaterEquivalentThickness"
    },
    "300A00D8" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "WedgeOrientation"
    },
    "300A00D9" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "IsocenterToWedgeTrayDistance"
    },
    "300A00DA" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SourceToWedgeTrayDistance"
    },
    "300A00DB" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "WedgeThinEdgePosition"
    },
    "300A00DC" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "BolusID"
    },
    "300A00DD" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "BolusDescription"
    },
    "300A00E0" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfCompensators"
    },
    "300A00E1" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "MaterialID"
    },
    "300A00E2" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TotalCompensatorTrayFactor"
    },
    "300A00E3" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CompensatorSequence"
    },
    "300A00E4" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "CompensatorNumber"
    },
    "300A00E5" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "CompensatorID"
    },
    "300A00E6" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SourceToCompensatorTrayDistance"
    },
    "300A00E7" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "CompensatorRows"
    },
    "300A00E8" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "CompensatorColumns"
    },
    "300A00E9" : {
        "VR" : "DS",
        "VM" : "2",
        "KEYWORD" : "CompensatorPixelSpacing"
    },
    "300A00EA" : {
        "VR" : "DS",
        "VM" : "2",
        "KEYWORD" : "CompensatorPosition"
    },
    "300A00EB" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "CompensatorTransmissionData"
    },
    "300A00EC" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "CompensatorThicknessData"
    },
    "300A00ED" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfBoli"
    },
    "300A00EE" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CompensatorType"
    },
    "300A00F0" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfBlocks"
    },
    "300A00F2" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TotalBlockTrayFactor"
    },
    "300A00F3" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "TotalBlockTrayWaterEquivalentThickness"
    },
    "300A00F4" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BlockSequence"
    },
    "300A00F5" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "BlockTrayID"
    },
    "300A00F6" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SourceToBlockTrayDistance"
    },
    "300A00F7" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "IsocenterToBlockTrayDistance"
    },
    "300A00F8" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BlockType"
    },
    "300A00F9" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "AccessoryCode"
    },
    "300A00FA" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BlockDivergence"
    },
    "300A00FB" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BlockMountingPosition"
    },
    "300A00FC" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "BlockNumber"
    },
    "300A00FE" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "BlockName"
    },
    "300A0100" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "BlockThickness"
    },
    "300A0102" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "BlockTransmission"
    },
    "300A0104" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "BlockNumberOfPoints"
    },
    "300A0106" : {
        "VR" : "DS",
        "VM" : "2-2n",
        "KEYWORD" : "BlockData"
    },
    "300A0107" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ApplicatorSequence"
    },
    "300A0108" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "ApplicatorID"
    },
    "300A0109" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ApplicatorType"
    },
    "300A010A" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ApplicatorDescription"
    },
    "300A010C" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "CumulativeDoseReferenceCoefficient"
    },
    "300A010E" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "FinalCumulativeMetersetWeight"
    },
    "300A0110" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfControlPoints"
    },
    "300A0111" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ControlPointSequence"
    },
    "300A0112" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ControlPointIndex"
    },
    "300A0114" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "NominalBeamEnergy"
    },
    "300A0115" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "DoseRateSet"
    },
    "300A0116" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "WedgePositionSequence"
    },
    "300A0118" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "WedgePosition"
    },
    "300A011A" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BeamLimitingDevicePositionSequence"
    },
    "300A011C" : {
        "VR" : "DS",
        "VM" : "2-2n",
        "KEYWORD" : "LeafJawPositions"
    },
    "300A011E" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "GantryAngle"
    },
    "300A011F" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "GantryRotationDirection"
    },
    "300A0120" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "BeamLimitingDeviceAngle"
    },
    "300A0121" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BeamLimitingDeviceRotationDirection"
    },
    "300A0122" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "PatientSupportAngle"
    },
    "300A0123" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PatientSupportRotationDirection"
    },
    "300A0124" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TableTopEccentricAxisDistance"
    },
    "300A0125" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TableTopEccentricAngle"
    },
    "300A0126" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TableTopEccentricRotationDirection"
    },
    "300A0128" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TableTopVerticalPosition"
    },
    "300A0129" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TableTopLongitudinalPosition"
    },
    "300A012A" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TableTopLateralPosition"
    },
    "300A012C" : {
        "VR" : "DS",
        "VM" : "3",
        "KEYWORD" : "IsocenterPosition"
    },
    "300A012E" : {
        "VR" : "DS",
        "VM" : "3",
        "KEYWORD" : "SurfaceEntryPoint"
    },
    "300A0130" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SourceToSurfaceDistance"
    },
    "300A0134" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "CumulativeMetersetWeight"
    },
    "300A0140" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "TableTopPitchAngle"
    },
    "300A0142" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TableTopPitchRotationDirection"
    },
    "300A0144" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "TableTopRollAngle"
    },
    "300A0146" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TableTopRollRotationDirection"
    },
    "300A0148" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "HeadFixationAngle"
    },
    "300A014A" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "GantryPitchAngle"
    },
    "300A014C" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "GantryPitchRotationDirection"
    },
    "300A014E" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "GantryPitchAngleTolerance"
    },
    "300A0180" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PatientSetupSequence"
    },
    "300A0182" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "PatientSetupNumber"
    },
    "300A0183" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PatientSetupLabel"
    },
    "300A0184" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PatientAdditionalPosition"
    },
    "300A0190" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "FixationDeviceSequence"
    },
    "300A0192" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "FixationDeviceType"
    },
    "300A0194" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "FixationDeviceLabel"
    },
    "300A0196" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "FixationDeviceDescription"
    },
    "300A0198" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "FixationDevicePosition"
    },
    "300A0199" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "FixationDevicePitchAngle"
    },
    "300A019A" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "FixationDeviceRollAngle"
    },
    "300A01A0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ShieldingDeviceSequence"
    },
    "300A01A2" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ShieldingDeviceType"
    },
    "300A01A4" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "ShieldingDeviceLabel"
    },
    "300A01A6" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "ShieldingDeviceDescription"
    },
    "300A01A8" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "ShieldingDevicePosition"
    },
    "300A01B0" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SetupTechnique"
    },
    "300A01B2" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "SetupTechniqueDescription"
    },
    "300A01B4" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SetupDeviceSequence"
    },
    "300A01B6" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SetupDeviceType"
    },
    "300A01B8" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "SetupDeviceLabel"
    },
    "300A01BA" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "SetupDeviceDescription"
    },
    "300A01BC" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SetupDeviceParameter"
    },
    "300A01D0" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "SetupReferenceDescription"
    },
    "300A01D2" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TableTopVerticalSetupDisplacement"
    },
    "300A01D4" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TableTopLongitudinalSetupDisplacement"
    },
    "300A01D6" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TableTopLateralSetupDisplacement"
    },
    "300A0200" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BrachyTreatmentTechnique"
    },
    "300A0202" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BrachyTreatmentType"
    },
    "300A0206" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "TreatmentMachineSequence"
    },
    "300A0210" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SourceSequence"
    },
    "300A0212" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "SourceNumber"
    },
    "300A0214" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SourceType"
    },
    "300A0216" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SourceManufacturer"
    },
    "300A0218" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ActiveSourceDiameter"
    },
    "300A021A" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ActiveSourceLength"
    },
    "300A0222" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SourceEncapsulationNominalThickness"
    },
    "300A0224" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SourceEncapsulationNominalTransmission"
    },
    "300A0226" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SourceIsotopeName"
    },
    "300A0228" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SourceIsotopeHalfLife"
    },
    "300A0229" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SourceStrengthUnits"
    },
    "300A022A" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ReferenceAirKermaRate"
    },
    "300A022B" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SourceStrength"
    },
    "300A022C" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "SourceStrengthReferenceDate"
    },
    "300A022E" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "SourceStrengthReferenceTime"
    },
    "300A0230" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ApplicationSetupSequence"
    },
    "300A0232" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ApplicationSetupType"
    },
    "300A0234" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ApplicationSetupNumber"
    },
    "300A0236" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ApplicationSetupName"
    },
    "300A0238" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ApplicationSetupManufacturer"
    },
    "300A0240" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "TemplateNumber"
    },
    "300A0242" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "TemplateType"
    },
    "300A0244" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "TemplateName"
    },
    "300A0250" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TotalReferenceAirKerma"
    },
    "300A0260" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BrachyAccessoryDeviceSequence"
    },
    "300A0262" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "BrachyAccessoryDeviceNumber"
    },
    "300A0263" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "BrachyAccessoryDeviceID"
    },
    "300A0264" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "BrachyAccessoryDeviceType"
    },
    "300A0266" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "BrachyAccessoryDeviceName"
    },
    "300A026A" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "BrachyAccessoryDeviceNominalThickness"
    },
    "300A026C" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "BrachyAccessoryDeviceNominalTransmission"
    },
    "300A0280" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ChannelSequence"
    },
    "300A0282" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ChannelNumber"
    },
    "300A0284" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ChannelLength"
    },
    "300A0286" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ChannelTotalTime"
    },
    "300A0288" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SourceMovementType"
    },
    "300A028A" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfPulses"
    },
    "300A028C" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "PulseRepetitionInterval"
    },
    "300A0290" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "SourceApplicatorNumber"
    },
    "300A0291" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "SourceApplicatorID"
    },
    "300A0292" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "SourceApplicatorType"
    },
    "300A0294" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SourceApplicatorName"
    },
    "300A0296" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SourceApplicatorLength"
    },
    "300A0298" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "SourceApplicatorManufacturer"
    },
    "300A029C" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SourceApplicatorWallNominalThickness"
    },
    "300A029E" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SourceApplicatorWallNominalTransmission"
    },
    "300A02A0" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "SourceApplicatorStepSize"
    },
    "300A02A2" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "TransferTubeNumber"
    },
    "300A02A4" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "TransferTubeLength"
    },
    "300A02B0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ChannelShieldSequence"
    },
    "300A02B2" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ChannelShieldNumber"
    },
    "300A02B3" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "ChannelShieldID"
    },
    "300A02B4" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ChannelShieldName"
    },
    "300A02B8" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ChannelShieldNominalThickness"
    },
    "300A02BA" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ChannelShieldNominalTransmission"
    },
    "300A02C8" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "FinalCumulativeTimeWeight"
    },
    "300A02D0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BrachyControlPointSequence"
    },
    "300A02D2" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ControlPointRelativePosition"
    },
    "300A02D4" : {
        "VR" : "DS",
        "VM" : "3",
        "KEYWORD" : "ControlPoint3DPosition"
    },
    "300A02D6" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "CumulativeTimeWeight"
    },
    "300A02E0" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CompensatorDivergence"
    },
    "300A02E1" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CompensatorMountingPosition"
    },
    "300A02E2" : {
        "VR" : "DS",
        "VM" : "1-n",
        "KEYWORD" : "SourceToCompensatorDistance"
    },
    "300A02E3" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "TotalCompensatorTrayWaterEquivalentThickness"
    },
    "300A02E4" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "IsocenterToCompensatorTrayDistance"
    },
    "300A02E5" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "CompensatorColumnOffset"
    },
    "300A02E6" : {
        "VR" : "FL",
        "VM" : "1-n",
        "KEYWORD" : "IsocenterToCompensatorDistances"
    },
    "300A02E7" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "CompensatorRelativeStoppingPowerRatio"
    },
    "300A02E8" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "CompensatorMillingToolDiameter"
    },
    "300A02EA" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IonRangeCompensatorSequence"
    },
    "300A02EB" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "CompensatorDescription"
    },
    "300A0302" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "RadiationMassNumber"
    },
    "300A0304" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "RadiationAtomicNumber"
    },
    "300A0306" : {
        "VR" : "SS",
        "VM" : "1",
        "KEYWORD" : "RadiationChargeState"
    },
    "300A0308" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ScanMode"
    },
    "300A030A" : {
        "VR" : "FL",
        "VM" : "2",
        "KEYWORD" : "VirtualSourceAxisDistances"
    },
    "300A030C" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SnoutSequence"
    },
    "300A030D" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "SnoutPosition"
    },
    "300A030F" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "SnoutID"
    },
    "300A0312" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfRangeShifters"
    },
    "300A0314" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RangeShifterSequence"
    },
    "300A0316" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "RangeShifterNumber"
    },
    "300A0318" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "RangeShifterID"
    },
    "300A0320" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RangeShifterType"
    },
    "300A0322" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "RangeShifterDescription"
    },
    "300A0330" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfLateralSpreadingDevices"
    },
    "300A0332" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "LateralSpreadingDeviceSequence"
    },
    "300A0334" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "LateralSpreadingDeviceNumber"
    },
    "300A0336" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "LateralSpreadingDeviceID"
    },
    "300A0338" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "LateralSpreadingDeviceType"
    },
    "300A033A" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "LateralSpreadingDeviceDescription"
    },
    "300A033C" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "LateralSpreadingDeviceWaterEquivalentThickness"
    },
    "300A0340" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfRangeModulators"
    },
    "300A0342" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RangeModulatorSequence"
    },
    "300A0344" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "RangeModulatorNumber"
    },
    "300A0346" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "RangeModulatorID"
    },
    "300A0348" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RangeModulatorType"
    },
    "300A034A" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "RangeModulatorDescription"
    },
    "300A034C" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "BeamCurrentModulationID"
    },
    "300A0350" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PatientSupportType"
    },
    "300A0352" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "PatientSupportID"
    },
    "300A0354" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "PatientSupportAccessoryCode"
    },
    "300A0356" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "FixationLightAzimuthalAngle"
    },
    "300A0358" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "FixationLightPolarAngle"
    },
    "300A035A" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "MetersetRate"
    },
    "300A0360" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RangeShifterSettingsSequence"
    },
    "300A0362" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "RangeShifterSetting"
    },
    "300A0364" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "IsocenterToRangeShifterDistance"
    },
    "300A0366" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "RangeShifterWaterEquivalentThickness"
    },
    "300A0370" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "LateralSpreadingDeviceSettingsSequence"
    },
    "300A0372" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "LateralSpreadingDeviceSetting"
    },
    "300A0374" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "IsocenterToLateralSpreadingDeviceDistance"
    },
    "300A0380" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RangeModulatorSettingsSequence"
    },
    "300A0382" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "RangeModulatorGatingStartValue"
    },
    "300A0384" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "RangeModulatorGatingStopValue"
    },
    "300A0386" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "RangeModulatorGatingStartWaterEquivalentThickness"
    },
    "300A0388" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "RangeModulatorGatingStopWaterEquivalentThickness"
    },
    "300A038A" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "IsocenterToRangeModulatorDistance"
    },
    "300A0390" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "ScanSpotTuneID"
    },
    "300A0392" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfScanSpotPositions"
    },
    "300A0394" : {
        "VR" : "FL",
        "VM" : "1-n",
        "KEYWORD" : "ScanSpotPositionMap"
    },
    "300A0396" : {
        "VR" : "FL",
        "VM" : "1-n",
        "KEYWORD" : "ScanSpotMetersetWeights"
    },
    "300A0398" : {
        "VR" : "FL",
        "VM" : "2",
        "KEYWORD" : "ScanningSpotSize"
    },
    "300A039A" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfPaintings"
    },
    "300A03A0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IonToleranceTableSequence"
    },
    "300A03A2" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IonBeamSequence"
    },
    "300A03A4" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IonBeamLimitingDeviceSequence"
    },
    "300A03A6" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IonBlockSequence"
    },
    "300A03A8" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IonControlPointSequence"
    },
    "300A03AA" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IonWedgeSequence"
    },
    "300A03AC" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "IonWedgePositionSequence"
    },
    "300A0401" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedSetupImageSequence"
    },
    "300A0402" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "SetupImageComment"
    },
    "300A0410" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MotionSynchronizationSequence"
    },
    "300A0412" : {
        "VR" : "FL",
        "VM" : "3",
        "KEYWORD" : "ControlPointOrientation"
    },
    "300A0420" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "GeneralAccessorySequence"
    },
    "300A0421" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "GeneralAccessoryID"
    },
    "300A0422" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "GeneralAccessoryDescription"
    },
    "300A0423" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "GeneralAccessoryType"
    },
    "300A0424" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "GeneralAccessoryNumber"
    },
    "300A0431" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ApplicatorGeometrySequence"
    },
    "300A0432" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ApplicatorApertureShape"
    },
    "300A0433" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "ApplicatorOpening"
    },
    "300A0434" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "ApplicatorOpeningX"
    },
    "300A0435" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "ApplicatorOpeningY"
    },
    "300A0436" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "SourceToApplicatorMountingPositionDistance"
    },
    "300C0002" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedRTPlanSequence"
    },
    "300C0004" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedBeamSequence"
    },
    "300C0006" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedBeamNumber"
    },
    "300C0007" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedReferenceImageNumber"
    },
    "300C0008" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "StartCumulativeMetersetWeight"
    },
    "300C0009" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "EndCumulativeMetersetWeight"
    },
    "300C000A" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedBrachyApplicationSetupSequence"
    },
    "300C000C" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedBrachyApplicationSetupNumber"
    },
    "300C000E" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedSourceNumber"
    },
    "300C0020" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedFractionGroupSequence"
    },
    "300C0022" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedFractionGroupNumber"
    },
    "300C0040" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedVerificationImageSequence"
    },
    "300C0042" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedReferenceImageSequence"
    },
    "300C0050" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedDoseReferenceSequence"
    },
    "300C0051" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedDoseReferenceNumber"
    },
    "300C0055" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BrachyReferencedDoseReferenceSequence"
    },
    "300C0060" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedStructureSetSequence"
    },
    "300C006A" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedPatientSetupNumber"
    },
    "300C0080" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedDoseSequence"
    },
    "300C00A0" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedToleranceTableNumber"
    },
    "300C00B0" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedBolusSequence"
    },
    "300C00C0" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedWedgeNumber"
    },
    "300C00D0" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedCompensatorNumber"
    },
    "300C00E0" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedBlockNumber"
    },
    "300C00F0" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedControlPointIndex"
    },
    "300C00F2" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedControlPointSequence"
    },
    "300C00F4" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedStartControlPointIndex"
    },
    "300C00F6" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedStopControlPointIndex"
    },
    "300C0100" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedRangeShifterNumber"
    },
    "300C0102" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedLateralSpreadingDeviceNumber"
    },
    "300C0104" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ReferencedRangeModulatorNumber"
    },
    "300E0002" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ApprovalStatus"
    },
    "300E0004" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "ReviewDate"
    },
    "300E0005" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "ReviewTime"
    },
    "300E0008" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "ReviewerName"
    },
    "40000010" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "Arbitrary"
    },
    "40004000" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "TextComments"
    },
    "40080040" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "ResultsID"
    },
    "40080042" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ResultsIDIssuer"
    },
    "40080050" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ReferencedInterpretationSequence"
    },
    "400800FF" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ReportProductionStatusTrial"
    },
    "40080100" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "InterpretationRecordedDate"
    },
    "40080101" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "InterpretationRecordedTime"
    },
    "40080102" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "InterpretationRecorder"
    },
    "40080103" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ReferenceToRecordedSound"
    },
    "40080108" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "InterpretationTranscriptionDate"
    },
    "40080109" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "InterpretationTranscriptionTime"
    },
    "4008010A" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "InterpretationTranscriber"
    },
    "4008010B" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "InterpretationText"
    },
    "4008010C" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "InterpretationAuthor"
    },
    "40080111" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "InterpretationApproverSequence"
    },
    "40080112" : {
        "VR" : "DA",
        "VM" : "1",
        "KEYWORD" : "InterpretationApprovalDate"
    },
    "40080113" : {
        "VR" : "TM",
        "VM" : "1",
        "KEYWORD" : "InterpretationApprovalTime"
    },
    "40080114" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "PhysicianApprovingInterpretation"
    },
    "40080115" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "InterpretationDiagnosisDescription"
    },
    "40080117" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "InterpretationDiagnosisCodeSequence"
    },
    "40080118" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ResultsDistributionListSequence"
    },
    "40080119" : {
        "VR" : "PN",
        "VM" : "1",
        "KEYWORD" : "DistributionName"
    },
    "4008011A" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "DistributionAddress"
    },
    "40080200" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "InterpretationID"
    },
    "40080202" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "InterpretationIDIssuer"
    },
    "40080210" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "InterpretationTypeID"
    },
    "40080212" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "InterpretationStatusID"
    },
    "40080300" : {
        "VR" : "ST",
        "VM" : "1",
        "KEYWORD" : "Impressions"
    },
    "40084000" : {
        "VR" : "ST",
        "VM" : "1 ",
        "KEYWORD" : "ResultsComments"
    },
    "40100001" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "LowEnergyDetectors"
    },
    "40100002" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "HighEnergyDetectors"
    },
    "40100004" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DetectorGeometrySequence"
    },
    "40101001" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ThreatROIVoxelSequence"
    },
    "40101004" : {
        "VR" : "FL",
        "VM" : "3",
        "KEYWORD" : "ThreatROIBase"
    },
    "40101005" : {
        "VR" : "FL",
        "VM" : "3",
        "KEYWORD" : "ThreatROIExtents"
    },
    "40101006" : {
        "VR" : "OB",
        "VM" : "1",
        "KEYWORD" : "ThreatROIBitmap"
    },
    "40101007" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "RouteSegmentID"
    },
    "40101008" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "GantryType"
    },
    "40101009" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OOIOwnerType"
    },
    "4010100A" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "RouteSegmentSequence"
    },
    "40101010" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "PotentialThreatObjectID"
    },
    "40101011" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ThreatSequence"
    },
    "40101012" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ThreatCategory"
    },
    "40101013" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "ThreatCategoryDescription"
    },
    "40101014" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ATDAbilityAssessment"
    },
    "40101015" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ATDAssessmentFlag"
    },
    "40101016" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "ATDAssessmentProbability"
    },
    "40101017" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "Mass"
    },
    "40101018" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "Density"
    },
    "40101019" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "ZEffective"
    },
    "4010101A" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "BoardingPassID"
    },
    "4010101B" : {
        "VR" : "FL",
        "VM" : "3",
        "KEYWORD" : "CenterOfMass"
    },
    "4010101C" : {
        "VR" : "FL",
        "VM" : "3",
        "KEYWORD" : "CenterOfPTO"
    },
    "4010101D" : {
        "VR" : "FL",
        "VM" : "6-n",
        "KEYWORD" : "BoundingPolygon"
    },
    "4010101E" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "RouteSegmentStartLocationID"
    },
    "4010101F" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "RouteSegmentEndLocationID"
    },
    "40101020" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "RouteSegmentLocationIDType"
    },
    "40101021" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "AbortReason"
    },
    "40101023" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "VolumeOfPTO"
    },
    "40101024" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AbortFlag"
    },
    "40101025" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "RouteSegmentStartTime"
    },
    "40101026" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "RouteSegmentEndTime"
    },
    "40101027" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TDRType"
    },
    "40101028" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "InternationalRouteSegment"
    },
    "40101029" : {
        "VR" : "LO",
        "VM" : "1-n",
        "KEYWORD" : "ThreatDetectionAlgorithmandVersion"
    },
    "4010102A" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "AssignedLocation"
    },
    "4010102B" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "AlarmDecisionTime"
    },
    "40101031" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AlarmDecision"
    },
    "40101033" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfTotalObjects"
    },
    "40101034" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfAlarmObjects"
    },
    "40101037" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PTORepresentationSequence"
    },
    "40101038" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "ATDAssessmentSequence"
    },
    "40101039" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TIPType"
    },
    "4010103A" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "DICOSVersion"
    },
    "40101041" : {
        "VR" : "DT",
        "VM" : "1",
        "KEYWORD" : "OOIOwnerCreationTime"
    },
    "40101042" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OOIType"
    },
    "40101043" : {
        "VR" : "FL",
        "VM" : "3",
        "KEYWORD" : "OOISize"
    },
    "40101044" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "AcquisitionStatus"
    },
    "40101045" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "BasisMaterialsCodeSequence"
    },
    "40101046" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "PhantomType"
    },
    "40101047" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "OOIOwnerSequence"
    },
    "40101048" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "ScanType"
    },
    "40101051" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ItineraryID"
    },
    "40101052" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "ItineraryIDType"
    },
    "40101053" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "ItineraryIDAssigningAuthority"
    },
    "40101054" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "RouteID"
    },
    "40101055" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "RouteIDAssigningAuthority"
    },
    "40101056" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "InboundArrivalType"
    },
    "40101058" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "CarrierID"
    },
    "40101059" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CarrierIDAssigningAuthority"
    },
    "40101060" : {
        "VR" : "FL",
        "VM" : "3",
        "KEYWORD" : "SourceOrientation"
    },
    "40101061" : {
        "VR" : "FL",
        "VM" : "3",
        "KEYWORD" : "SourcePosition"
    },
    "40101062" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "BeltHeight"
    },
    "40101064" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "AlgorithmRoutingCodeSequence"
    },
    "40101067" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TransportClassification"
    },
    "40101068" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "OOITypeDescriptor"
    },
    "40101069" : {
        "VR" : "FL",
        "VM" : "1",
        "KEYWORD" : "TotalProcessingTime"
    },
    "4010106C" : {
        "VR" : "OB",
        "VM" : "1",
        "KEYWORD" : "DetectorCalibrationData"
    },
    "4FFE0001" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "MACParametersSequence"
    },
    "50xx0005" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "CurveDimensions"
    },
    "50xx0010" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfPoints"
    },
    "50xx0020" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "TypeOfData"
    },
    "50xx0022" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "CurveDescription"
    },
    "50xx0030" : {
        "VR" : "SH",
        "VM" : "1-n",
        "KEYWORD" : "AxisUnits"
    },
    "50xx0040" : {
        "VR" : "SH",
        "VM" : "1-n",
        "KEYWORD" : "AxisLabels"
    },
    "50xx0103" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "DataValueRepresentation"
    },
    "50xx0104" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "MinimumCoordinateValue"
    },
    "50xx0105" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "MaximumCoordinateValue"
    },
    "50xx0106" : {
        "VR" : "SH",
        "VM" : "1-n",
        "KEYWORD" : "CurveRange"
    },
    "50xx0110" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "CurveDataDescriptor"
    },
    "50xx0112" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "CoordinateStartValue"
    },
    "50xx0114" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "CoordinateStepValue"
    },
    "50xx1001" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "CurveActivationLayer"
    },
    "50xx2000" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "AudioType"
    },
    "50xx2002" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "AudioSampleFormat"
    },
    "50xx2004" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "NumberOfChannels"
    },
    "50xx2006" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "NumberOfSamples"
    },
    "50xx2008" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "SampleRate"
    },
    "50xx200A" : {
        "VR" : "UL",
        "VM" : "1",
        "KEYWORD" : "TotalTime"
    },
    "50xx200C" : {
        "VR" : "OW|OB",
        "VM" : "1",
        "KEYWORD" : "AudioSampleData"
    },
    "50xx200E" : {
        "VR" : "LT",
        "VM" : "1 ",
        "KEYWORD" : "AudioComments"
    },
    "50xx2500" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "CurveLabel"
    },
    "50xx2600" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "CurveReferencedOverlaySequence"
    },
    "50xx2610" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "CurveReferencedOverlayGroup"
    },
    "50xx3000" : {
        "VR" : "OW|OB",
        "VM" : "1",
        "KEYWORD" : "CurveData"
    },
    "52009229" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "SharedFunctionalGroupsSequence"
    },
    "52009230" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "PerFrameFunctionalGroupsSequence"
    },
    "54000100" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "WaveformSequence"
    },
    "54000110" : {
        "VR" : "OB|OW",
        "VM" : "1",
        "KEYWORD" : "ChannelMinimumValue"
    },
    "54000112" : {
        "VR" : "OB|OW",
        "VM" : "1",
        "KEYWORD" : "ChannelMaximumValue"
    },
    "54001004" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "WaveformBitsAllocated"
    },
    "54001006" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "WaveformSampleInterpretation"
    },
    "5400100A" : {
        "VR" : "OB|OW",
        "VM" : "1",
        "KEYWORD" : "WaveformPaddingValue"
    },
    "54001010" : {
        "VR" : "OB|OW",
        "VM" : "1",
        "KEYWORD" : "WaveformData"
    },
    "56000010" : {
        "VR" : "OF",
        "VM" : "1",
        "KEYWORD" : "FirstOrderPhaseCorrectionAngle"
    },
    "56000020" : {
        "VR" : "OF",
        "VM" : "1",
        "KEYWORD" : "SpectroscopyData"
    },
    "60xx0010" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "OverlayRows"
    },
    "60xx0011" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "OverlayColumns"
    },
    "60xx0012" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "OverlayPlanes"
    },
    "60xx0015" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "NumberOfFramesInOverlay"
    },
    "60xx0022" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "OverlayDescription"
    },
    "60xx0040" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OverlayType"
    },
    "60xx0045" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "OverlaySubtype"
    },
    "60xx0050" : {
        "VR" : "SS",
        "VM" : "2",
        "KEYWORD" : "OverlayOrigin"
    },
    "60xx0051" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "ImageFrameOrigin"
    },
    "60xx0052" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "OverlayPlaneOrigin"
    },
    "60xx0060" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OverlayCompressionCode"
    },
    "60xx0061" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "OverlayCompressionOriginator"
    },
    "60xx0062" : {
        "VR" : "SH",
        "VM" : "1",
        "KEYWORD" : "OverlayCompressionLabel"
    },
    "60xx0063" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OverlayCompressionDescription"
    },
    "60xx0066" : {
        "VR" : "AT",
        "VM" : "1-n",
        "KEYWORD" : "OverlayCompressionStepPointers"
    },
    "60xx0068" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "OverlayRepeatInterval"
    },
    "60xx0069" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "OverlayBitsGrouped"
    },
    "60xx0100" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "OverlayBitsAllocated"
    },
    "60xx0102" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "OverlayBitPosition"
    },
    "60xx0110" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OverlayFormat"
    },
    "60xx0200" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "OverlayLocation"
    },
    "60xx0800" : {
        "VR" : "CS",
        "VM" : "1-n",
        "KEYWORD" : "OverlayCodeLabel"
    },
    "60xx0802" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "OverlayNumberOfTables"
    },
    "60xx0803" : {
        "VR" : "AT",
        "VM" : "1-n",
        "KEYWORD" : "OverlayCodeTableLocation"
    },
    "60xx0804" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "OverlayBitsForCodeWord"
    },
    "60xx1001" : {
        "VR" : "CS",
        "VM" : "1",
        "KEYWORD" : "OverlayActivationLayer"
    },
    "60xx1100" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "OverlayDescriptorGray"
    },
    "60xx1101" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "OverlayDescriptorRed"
    },
    "60xx1102" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "OverlayDescriptorGreen"
    },
    "60xx1103" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "OverlayDescriptorBlue"
    },
    "60xx1200" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "OverlaysGray"
    },
    "60xx1201" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "OverlaysRed"
    },
    "60xx1202" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "OverlaysGreen"
    },
    "60xx1203" : {
        "VR" : "US",
        "VM" : "1-n",
        "KEYWORD" : "OverlaysBlue"
    },
    "60xx1301" : {
        "VR" : "IS",
        "VM" : "1",
        "KEYWORD" : "ROIArea"
    },
    "60xx1302" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ROIMean"
    },
    "60xx1303" : {
        "VR" : "DS",
        "VM" : "1",
        "KEYWORD" : "ROIStandardDeviation"
    },
    "60xx1500" : {
        "VR" : "LO",
        "VM" : "1",
        "KEYWORD" : "OverlayLabel"
    },
    "60xx3000" : {
        "VR" : "OB|OW",
        "VM" : "1",
        "KEYWORD" : "OverlayData"
    },
    "60xx4000" : {
        "VR" : "LT",
        "VM" : "1",
        "KEYWORD" : "OverlayComments"
    },
    "7FE00010" : {
        "VR" : "OW|OB",
        "VM" : "1",
        "KEYWORD" : "PixelData"
    },
    "7FE00020" : {
        "VR" : "OW",
        "VM" : "1",
        "KEYWORD" : "CoefficientsSDVN"
    },
    "7FE00030" : {
        "VR" : "OW",
        "VM" : "1",
        "KEYWORD" : "CoefficientsSDHN"
    },
    "7FE00040" : {
        "VR" : "OW",
        "VM" : "1",
        "KEYWORD" : "CoefficientsSDDN"
    },
    "7Fxx0010" : {
        "VR" : "OW|OB",
        "VM" : "1",
        "KEYWORD" : "VariablePixelData"
    },
    "7Fxx0011" : {
        "VR" : "US",
        "VM" : "1",
        "KEYWORD" : "VariableNextDataGroup"
    },
    "7Fxx0020" : {
        "VR" : "OW",
        "VM" : "1",
        "KEYWORD" : "VariableCoefficientsSDVN"
    },
    "7Fxx0030" : {
        "VR" : "OW",
        "VM" : "1",
        "KEYWORD" : "VariableCoefficientsSDHN"
    },
    "7Fxx0040" : {
        "VR" : "OW",
        "VM" : "1",
        "KEYWORD" : "VariableCoefficientsSDDN"
    },
    "FFFAFFFA" : {
        "VR" : "SQ",
        "VM" : "1",
        "KEYWORD" : "DigitalSignaturesSequence"
    },
    "FFFCFFFC" : {
        "VR" : "OB",
        "VM" : "1",
        "KEYWORD" : "DataSetTrailingPadding"
    },
    "FFFEE000" : {
        "VR" : "",
        "VM" : "1",
        "KEYWORD" : "Item"
    },
    "FFFEE00D" : {
        "VR" : "",
        "VM" : "1",
        "KEYWORD" : "ItemDelimitationItem"
    },
    "FFFEE0DD" : {
        "VR" : "",
        "VM" : "1",
        "KEYWORD" : "SequenceDelimitationItem"
    }
};

// get keyword
function keywordOf(tag) {
    var key = dictionary[tag].KEYWORD;
    return key;
}
function getVR(tag) {
    var vr = dictionary[tag].VR;
    console.log("vr " + vr);
    return vr;
}
function tagOf(keyword) {
    for (i = 0; i < dictionary.size(); i++) {
        if (dictionary[i].KEYWORD == keyword)
            return dictionairy[i].name;
    }
}