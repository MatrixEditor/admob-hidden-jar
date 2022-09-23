package io.github.matrixeditor.shjartest;

import io.github.matrixeditor.shjar.ShadowJar;
import io.github.matrixeditor.shjar.SharedClass;

import java.security.GeneralSecurityException;

public class SharedClassTest {

    public static final String[][] values = {
            {"3K8ELf0l97OoJkLZ8w0j1a7PyOLXhiU05fzwfJl+q5R78K5HrzBW+Y6Q9dHwMhs9", "b/KmM8qxPHQsR3T0CZ/F1d5gVFlEyl3WbrFOrFCfLkc="},
            {"y/7Ec+Kioax0ZYAEH/NiuXIsrXhfXjhFi2F/nP/uGkIQw7jUhBGY0W6baOqNedZ3", "0cd3YaSrpW7P/AkpW4/onj4SWvDEhcgrou0hv6tCD+s="},
            {"7OKu9eBgadNAt0os6Yom9Fc4Z+g/QG9jOe+s+JAGcO8yKBHrGX1guBev/xXHxnY1", "oyXxJz1/0whZ1sjpKwx6gZrLwCk4v4rbcD6dwdNy6Cs="},
            {"7KqrGXdk/af3XqeV7Kdf9PbIbh3C1JbHd2XfO7vMbAGonJ11eSDtjbVaiMZyTrse", "J+RI1JNo03xly9SRWO99cvEQhgdX6x+RL8PvxYQsPic="},
            {"poItj6KYBt5BdnGUyKVpTqoJe9uUrbj0v36XCagBsYjc7oBvjejRtfaJj7hby90a", "qrGvg6pF9No4qMDhGfUiE0qkqx8DIEI1ObOP4v8z9Vw="},
            {"ljgMduD5kKPhK6qoocDB5WikkD/CX1UhLGscwfIdkLacCDlfdaQLvcdlCLYlVWEY", "PQw299M+gyZAcirIo44nMA1T6aZFSbwBV2vuW7mK5no="},
            {"WFWMehHtsFVdDUEDmR6QBWCVOt7vhsukeVfF7GyA4nJfn/HGydHkg9TxbsV2ogHJ", "anxhUuNThpd2uUrKqPFlUd7Xx/0GNeeVxwSMkxx8/S8="},
            {"wFR9IVBeC3DxjLUHG4VrPvC6sFnF3i6EXAzgF2FV6F/P1tVIxRsZCqzBo3bMqAjw", "FngGcM4uMQo5eKR2FwSlMJnWgsaWh19tKknwKd4tbUk="},
            {"D/UUEAj6b2Mlzt93EqUc0ecxuQkFtjBjbwXxowRDAJrg0neoMkyf1TZxyfD/n/R+", "XHiP8GJo+im4O4z0srsKg+iMpuUpknn3IyfTXGI6thg="},
            {"Vd8F7XEjcopfPCqIo7UkH81gSF32pH8ICaByQHEDSARI1QZSSarfP6NUtOQ3X7FJ", "SC+voVwtFTxxanSuPBwO3gUspzEVAamsN8QrPnkhp0Y="},
            {"Oqhihr+ZvNwej7GnFvAZoFFN+/Dv8PoAOgFukKxDQRFkh0v6oq6svnLeRKh0PLMP", "7HSsIYWO+RMRoavsc1DDhaz46/CevFwPfCYPy/bWw2c="},
            {"z4IUOMEUJJwo87MrwW0oo5MuoOqDlnlQo6iV4pjz2VLab+vfSTsypn6zr+NoPrYU", "L28yi1GnuU8llvgZA5ti7bBAlAb8V1oFU13C+ZO3hWQ="},
            {"lzCo36nc7XBNxigvUEYksuBFsczDyb5Knn+0+OBVmAuGHrrIrxjgxEWtsLNUGdmh", "zUtdaly7f7RI0b3U/Ak3WbCr4NUUCFrNIppHHrQ+Up0="},
            {"+qJmoUGbbPFGT5cJqhcbMwFJCJD+zdsNFeU33HQsfBwCMUy52xVClKiKRlJSjJkA", "ZdvpwpF09ah9qMkbMMBkQbN3G9MUkP5Jgcz6nA07dkM="},
            {"NXaY56AtLwFcN+ekuuF2nJfd0TyeY8q5k/4jDq8aCw7dTKyZ2J/G7hTL+F9OtE+b", "IRJV9vmlQsYenNEwAJnRXRcX4jgQUq/mhLu9dLiWkF0="},
            {"8e8MBQUUwetJXHXD+OHa6GoKdEoeWcCKvJd/0LAqmt67vps6BIKdZS4FehPCaFQs", "SqEbpmL0PRd8OMIcAaTW+Qm/eIldjoaBC5+hIpZQMKI="},
            {"93FZcx3TGairtssuo4NaSvmwhnBdBRfmG43cez1s4nHLHJg1A/3UR3k+fui87zaI", "8yXffwi9xD4yRk9s3bzlCsp78O1cQWIN/y7cfZ1O0xQ="},
            {"085nDtgvdnF/9q+L3rbTJRP85nBeHpWsnwPQ5NbQuGUMVoHq922oSf9teCh818O7", "U6RyofmU5fe6F6Q76ZqotRxwCpQcx2ZA3KAIkgaYO1E="},
            {"Ob9vkrYwqwLnJveTtaSWm/WWJCjo/9DRtOCY3btkKa6pJtjMu6sI0iK41HSh10io", "UrT94Dq3ubetC7rQ64nVjqMQ53po9X61geGgrP+ILCU="}
    };

    public static void main(String[] args) throws GeneralSecurityException {
        ShadowJar jar = ShadowJar.getInstance();

        for (String[] names : values) {
            SharedClass cls = jar.addSharedClass(names[0], names[1]);
            System.out.println(cls);
        }
    }
}
