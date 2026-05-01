{ ... }:

{
  home = {
    file."Foo" = {
      text = ''
      ';
    };


    file."Bar" = {
      text = ''
      [GeneralOption]
      UseColor = true
      UpdateInterval = 1000
      ShowInfoMessages = true

      [HeaderOption]
      UseFahrenheit = false
      EncodeHideTimer = 3.000000e+01

      [ChartOption]
      ReverseChart = false

      [ProcessListOption]
      HideNvtopProcessList = false
      HideNvtopProcess = true
      SortOrder = descending
      SortBy = gpuRate
      '';
    };
  };
}
